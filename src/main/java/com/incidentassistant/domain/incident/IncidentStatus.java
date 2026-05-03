package com.incidentassistant.domain.incident;

/**
 * Incident lifecycle states per Phase 1a ({@code specs/phases/phase-1a-monolith-core/spec.md}).
 *
 * <p><strong>Canonical state machine (documentation):</strong> {@link #DRAFT} → {@link #OPEN} →
 * {@link #CLOSED}; {@link #CANCELLED} from {@link #DRAFT} or {@link #OPEN}; no transition from
 * {@link #CLOSED} back to {@link #OPEN} in v1; no transitions from {@link #CLOSED} or {@link
 * #CANCELLED} in v1.
 *
 * <p>Applying lifecycle transitions (HTTP {@code POST .../transitions} and the full illegal
 * transition matrix) is implemented in a later story; this enum carries allowed states only.
 */
public enum IncidentStatus {
  DRAFT,
  OPEN,
  CLOSED,
  CANCELLED
}
