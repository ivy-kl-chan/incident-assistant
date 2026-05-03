package com.incidentassistant.domain.incident;

/** Maps to HTTP 400 for invalid title, description length, or severity. */
public class IncidentValidationException extends RuntimeException {

  public IncidentValidationException(String message) {
    super(message);
  }
}
