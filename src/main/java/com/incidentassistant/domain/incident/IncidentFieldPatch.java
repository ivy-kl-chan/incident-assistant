package com.incidentassistant.domain.incident;

import java.util.Optional;

/**
 * Partial update of mutable incident fields. Empty optionals mean “leave unchanged” (HTTP PATCH
 * semantics).
 */
public record IncidentFieldPatch(
    Optional<String> title, Optional<String> description, Optional<IncidentSeverity> severity) {

  public static IncidentFieldPatch empty() {
    return new IncidentFieldPatch(Optional.empty(), Optional.empty(), Optional.empty());
  }

  public boolean isEmpty() {
    return title.isEmpty() && description.isEmpty() && severity.isEmpty();
  }
}
