package com.incidentassistant.incident.domain;

/** Illegal lifecycle transition; suitable for mapping to HTTP 409 later. */
public class IllegalIncidentTransitionException extends RuntimeException {

  public IllegalIncidentTransitionException(String message) {
    super(message);
  }
}
