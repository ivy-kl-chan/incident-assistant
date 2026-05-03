package com.incidentassistant.web.incident;

import java.util.List;

/** Paged list JSON for {@code GET /api/v1/incidents} (Phase 1a). */
public record PagedIncidentsResponse(
    List<IncidentSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages) {}
