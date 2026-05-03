package com.incidentassistant.web.incident;

import com.incidentassistant.domain.incident.Incident;
import java.time.Instant;
import java.util.UUID;

/** Item shape for {@code GET /api/v1/incidents} (Phase 1a). */
public record IncidentSummaryResponse(
    UUID id,
    String title,
    String status,
    String severity,
    String source,
    Instant createdAt,
    Instant updatedAt,
    long version) {

  static IncidentSummaryResponse from(Incident incident) {
    return new IncidentSummaryResponse(
        incident.id(),
        incident.title(),
        incident.status().name(),
        incident.severity().name(),
        incident.source().name(),
        incident.createdAt(),
        incident.updatedAt(),
        incident.version());
  }
}
