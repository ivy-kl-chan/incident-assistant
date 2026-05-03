package com.incidentassistant.domain.incident;

/** Optimistic lock failure; maps to HTTP 412 when surfaced via the API layer. */
public class IncidentStaleVersionException extends RuntimeException {

  public IncidentStaleVersionException(String message) {
    super(message);
  }
}
