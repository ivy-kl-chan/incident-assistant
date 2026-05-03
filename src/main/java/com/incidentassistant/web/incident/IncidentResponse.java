package com.incidentassistant.web.incident;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.incidentassistant.domain.incident.Incident;
import java.time.Instant;
import java.util.UUID;

/** Full incident JSON for {@code GET /api/v1/incidents/{id}} and {@code POST} response (Phase 1a). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IncidentResponse(
    UUID id,
    long version,
    String status,
    String title,
    String description,
    String severity,
    String source,
    Instant createdAt,
    Instant updatedAt,
    String transitionReason) {

  public static IncidentResponse from(Incident incident) {
    return new IncidentResponse(
        incident.id(),
        incident.version(),
        incident.status().name(),
        incident.title(),
        incident.description(),
        incident.severity().name(),
        incident.source().name(),
        incident.createdAt(),
        incident.updatedAt(),
        null);
  }
}
