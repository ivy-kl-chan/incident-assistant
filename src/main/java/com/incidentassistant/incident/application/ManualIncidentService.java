package com.incidentassistant.incident.application;

import com.incidentassistant.incident.domain.IncidentFieldRules;
import com.incidentassistant.incident.domain.IncidentSeverity;
import com.incidentassistant.incident.domain.IncidentSource;
import com.incidentassistant.incident.domain.IncidentStatus;
import com.incidentassistant.incident.domain.IncidentTransitionPolicy;
import com.incidentassistant.incident.persistence.IncidentEntity;
import com.incidentassistant.incident.persistence.IncidentEntityRepository;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manual incident persistence ({@link IncidentSource#MANUAL} only). Signal ingest must use a
 * separate path in Phase 1b.
 *
 * <p><strong>Optimistic {@code version}</strong> (foundation for ETag / If-Match): Hibernate
 * increments {@link IncidentEntity#getVersion()} <em>only</em> after a mutating operation
 * (insert or update) flushes successfully to the database. Failed flushes (including optimistic
 * lock conflicts) do not advance the version. New rows start at {@code 0} per {@code V1} default;
 * the first successful update moves the version to {@code 1}, and so on.
 */
@Service
public class ManualIncidentService {

  private final IncidentEntityRepository incidents;

  public ManualIncidentService(IncidentEntityRepository incidents) {
    this.incidents = incidents;
  }

  /**
   * Creates a new incident in {@link IncidentStatus#DRAFT} with {@link IncidentSource#MANUAL} — 1a
   * does not support creating as {@link IncidentStatus#OPEN} directly.
   */
  @Transactional
  public IncidentEntity createDraft(String title, String description, IncidentSeverity severity) {
    String t = IncidentFieldRules.requireValidTitle(title);
    String d = IncidentFieldRules.normalizeDescription(description);
    IncidentSeverity s = IncidentFieldRules.requireValidSeverity(severity);
    var id = UUID.randomUUID();
    var entity =
        new IncidentEntity(id, IncidentStatus.DRAFT, t, d, s, IncidentSource.MANUAL);
    return incidents.save(entity);
  }

  @Transactional
  public IncidentEntity updateContent(
      UUID id, long expectedVersion, String title, String description, IncidentSeverity severity) {
    IncidentEntity entity = loadForMutation(id, expectedVersion);
    IncidentFieldRules.requireEditableState(entity.getStatus());
    if (title != null) {
      entity.setTitle(IncidentFieldRules.requireValidTitle(title));
    }
    if (description != null) {
      entity.setDescription(IncidentFieldRules.normalizeDescription(description));
    }
    if (severity != null) {
      entity.setSeverity(IncidentFieldRules.requireValidSeverity(severity));
    }
    return incidents.save(entity);
  }

  @Transactional
  public IncidentEntity transition(UUID id, long expectedVersion, IncidentStatus to) {
    IncidentEntity entity = loadForMutation(id, expectedVersion);
    IncidentTransitionPolicy.requireValidTransition(entity.getStatus(), to);
    entity.setStatus(to);
    return incidents.save(entity);
  }

  private IncidentEntity loadForMutation(UUID id, long expectedVersion) {
    IncidentEntity entity =
        incidents
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("incident not found: " + id));
    if (entity.getVersion() != expectedVersion) {
      throw new OptimisticLockingFailureException("stale version for incident " + id);
    }
    if (entity.getSource() != IncidentSource.MANUAL) {
      throw new IllegalStateException("manual service cannot mutate non-MANUAL incident: " + id);
    }
    return entity;
  }

}
