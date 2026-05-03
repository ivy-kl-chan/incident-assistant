package com.incidentassistant.domain.incident;

/**
 * Incident lifecycle states per Phase 1a spec. Transition operations are handled in a later story.
 */
public enum IncidentStatus {
  DRAFT,
  OPEN,
  CLOSED,
  CANCELLED
}
