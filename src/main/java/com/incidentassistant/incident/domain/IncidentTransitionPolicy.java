package com.incidentassistant.incident.domain;

import java.util.Objects;

/**
 * State machine: {@code DRAFT → OPEN → CLOSED}; {@code CANCELLED} from {@code DRAFT} or {@code
 * OPEN}; no {@code OPEN} from {@code CLOSED} in v1 ({@code phase-1a-monolith-core/spec.md}).
 */
public final class IncidentTransitionPolicy {

  private IncidentTransitionPolicy() {}

  public static void requireValidTransition(IncidentStatus from, IncidentStatus to) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (from == to) {
      return;
    }
    boolean allowed =
        switch (from) {
          case DRAFT -> to == IncidentStatus.OPEN || to == IncidentStatus.CANCELLED;
          case OPEN -> to == IncidentStatus.CLOSED || to == IncidentStatus.CANCELLED;
          case CLOSED, CANCELLED -> false;
        };
    if (!allowed) {
      throw new IllegalIncidentTransitionException(
          "cannot transition from " + from + " to " + to);
    }
  }
}
