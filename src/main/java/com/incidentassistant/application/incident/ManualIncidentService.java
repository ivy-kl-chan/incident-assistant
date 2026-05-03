package com.incidentassistant.application.incident;

import com.incidentassistant.domain.incident.CreateManualIncidentCommand;
import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentConflictException;
import com.incidentassistant.domain.incident.IncidentFieldPatch;
import com.incidentassistant.domain.incident.IncidentNotFoundException;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStaleVersionException;
import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.IncidentValidationException;
import com.incidentassistant.domain.incident.IncidentValidator;
import com.incidentassistant.domain.incident.ManualIncidentRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Application service for manually created incidents ({@link IncidentSource#MANUAL} only). New rows
 * are created in {@link IncidentStatus#DRAFT}.
 *
 * <p><strong>Version:</strong> New incidents start at {@code version = 1}. Each successful field
 * update increments {@code version} by 1 in the repository (optimistic locking foundation for
 * {@code ETag} / {@code If-Match} in the HTTP layer).
 *
 * <p><strong>Timestamps:</strong> {@code created_at} and {@code updated_at} are set from {@link
 * Clock} on insert; {@code updated_at} advances on each successful mutation that updates the row.
 */
@Service
@ConditionalOnBean(JdbcTemplate.class)
public class ManualIncidentService {

  static final long INITIAL_VERSION = 1L;

  private final ManualIncidentRepository manualIncidentRepository;
  private final Clock clock;

  public ManualIncidentService(ManualIncidentRepository manualIncidentRepository, Clock clock) {
    this.manualIncidentRepository = manualIncidentRepository;
    this.clock = clock;
  }

  public Incident create(CreateManualIncidentCommand command) {
    CreateManualIncidentCommand validated = IncidentValidator.validateCreate(command);
    Instant now = clock.instant();
    UUID id = UUID.randomUUID();
    Incident incident =
        new Incident(
            id,
            INITIAL_VERSION,
            IncidentStatus.DRAFT,
            validated.title(),
            validated.description(),
            validated.severity(),
            IncidentSource.MANUAL,
            now,
            now);
    return manualIncidentRepository.insert(incident);
  }

  /**
   * Applies {@link IncidentFieldPatch} per Phase 1a PATCH rules: {@code title}, {@code description},
   * and {@code severity} may change only while status is {@link IncidentStatus#DRAFT} or {@link
   * IncidentStatus#OPEN}. If status is {@link IncidentStatus#CLOSED} or {@link
   * IncidentStatus#CANCELLED}, rejects with {@link IncidentConflictException} (HTTP 409).
   *
   * <p>Requires {@code expectedVersion} to match the stored row; otherwise {@link
   * IncidentStaleVersionException} (HTTP 412 when mapped).
   */
  public Incident updateFields(UUID id, long expectedVersion, IncidentFieldPatch patch) {
    if (patch.isEmpty()) {
      throw new IncidentValidationException("patch must contain at least one field");
    }
    IncidentFieldPatch validatedPatch = IncidentValidator.validatePatch(patch);

    Incident current =
        manualIncidentRepository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));

    if (current.version() != expectedVersion) {
      throw new IncidentStaleVersionException("incident version mismatch");
    }

    if (current.status() == IncidentStatus.CLOSED
        || current.status() == IncidentStatus.CANCELLED) {
      if (validatedPatch.title().isPresent()
          || validatedPatch.description().isPresent()
          || validatedPatch.severity().isPresent()) {
        throw new IncidentConflictException(
            "cannot update title, description, or severity when status is " + current.status());
      }
    }

    String newTitle = validatedPatch.title().orElse(current.title());
    String newDescription =
        validatedPatch.description().isPresent()
            ? validatedPatch.description().get()
            : current.description();
    IncidentSeverity newSeverity = validatedPatch.severity().orElse(current.severity());

    Instant now = clock.instant();
    return manualIncidentRepository
        .updateFields(id, expectedVersion, newTitle, newDescription, newSeverity, now)
        .orElseThrow(() -> new IncidentStaleVersionException("incident version mismatch"));
  }
}
