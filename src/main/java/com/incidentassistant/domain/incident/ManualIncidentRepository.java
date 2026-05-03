package com.incidentassistant.domain.incident;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for persisting manually created incidents only. Implementations must store {@code source =
 * MANUAL} and must not write Phase 1b columns.
 */
public interface ManualIncidentRepository {

  /**
   * Inserts a new row. The incident must have status {@link IncidentStatus#DRAFT}, source {@link
   * IncidentSource#MANUAL}, and initial {@code version} (see service Javadoc for normative
   * behavior). Sets {@code created_at} and {@code updated_at} in the database to the values on the
   * entity (caller supplies them, typically from {@link java.time.Clock}).
   */
  Incident insert(Incident incident);

  Optional<Incident> findById(UUID id);

  /**
   * Updates title, description, and severity for a manual incident when {@code expectedVersion}
   * matches. Increments {@code version} by 1 and sets {@code updated_at} in the row. Does not
   * change status. Returns empty if no row matched (wrong id, version, or non-{@code MANUAL} source).
   */
  Optional<Incident> updateFields(
      UUID id, long expectedVersion, String title, String description, IncidentSeverity severity, Instant updatedAt);
}
