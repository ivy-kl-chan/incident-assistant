package com.incidentassistant.incident.domain;

/** Validation failure suitable for mapping to HTTP 400 Problem Details later. */
public class IncidentValidationException extends RuntimeException {

  public IncidentValidationException(String message) {
    super(message);
  }
}
