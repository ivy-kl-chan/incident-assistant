package com.incidentassistant.domain.incident;

import java.util.List;

/** One page of manual incidents plus total row count for HTTP pagination. */
public record IncidentPage(List<Incident> items, long totalElements, int page, int size) {

  public int totalPages() {
    if (size <= 0) {
      return 0;
    }
    return (int) Math.ceil(totalElements / (double) size);
  }
}
