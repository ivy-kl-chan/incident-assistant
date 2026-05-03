package com.incidentassistant.domain.incident;

import java.util.UUID;

/** No row for the given id; maps to HTTP 404 in the API layer. */
public class IncidentNotFoundException extends RuntimeException {

  public IncidentNotFoundException(UUID id) {
    super("Incident not found: " + id);
  }
}
