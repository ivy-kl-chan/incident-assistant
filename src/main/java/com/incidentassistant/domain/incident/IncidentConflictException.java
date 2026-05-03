package com.incidentassistant.domain.incident;

/**
 * Conflicting mutation (e.g. updating title/description/severity when status is {@code CLOSED} or
 * {@code CANCELLED}). Maps to HTTP 409.
 */
public class IncidentConflictException extends RuntimeException {

  public IncidentConflictException(String message) {
    super(message);
  }
}
