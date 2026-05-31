package com.incidentassistant.domain.incident;

/**
 * Persistence source for an incident row. Phase 1a manual flows use {@link #MANUAL} only; signal rows
 * are Phase 1b.
 */
public enum IncidentSource {
  MANUAL,
  /** Phase 1b signal-ingest rows; not writable via Phase 1a manual APIs. */
  SIGNAL
}
