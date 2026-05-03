package com.incidentassistant.domain.incident;

import java.time.Instant;
import java.util.UUID;

public record Incident(
    UUID id,
    long version,
    IncidentStatus status,
    String title,
    String description,
    IncidentSeverity severity,
    IncidentSource source,
    Instant createdAt,
    Instant updatedAt) {}
