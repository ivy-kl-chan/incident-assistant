package com.incidentassistant.web.incident;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.incidentassistant.domain.incident.IncidentSeverity;

/** Request body for {@code POST /api/v1/incidents} (Phase 1a). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateIncidentRequest(String title, String description, IncidentSeverity severity) {}
