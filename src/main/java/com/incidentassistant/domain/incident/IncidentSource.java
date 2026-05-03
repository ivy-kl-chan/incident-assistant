package com.incidentassistant.domain.incident;

/**
 * Persistence source for an incident row. Phase 1a manual flows use {@link #MANUAL} only; signal rows
 * are Phase 1b.
 */
public enum IncidentSource {
  MANUAL
}
