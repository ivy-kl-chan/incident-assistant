package com.incidentassistant.web.incident;

import com.fasterxml.jackson.databind.JsonNode;
import com.incidentassistant.domain.incident.IncidentFieldPatch;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentValidationException;
import java.util.Iterator;
import java.util.Set;

/** Builds {@link IncidentFieldPatch} from PATCH JSON per Phase 1a {@code api-contract.md}. */
public final class PatchIncidentBodyParser {

  private static final Set<String> ALLOWED_KEYS = Set.of("title", "description", "severity");

  private PatchIncidentBodyParser() {}

  public static IncidentFieldPatch parse(JsonNode root) {
    if (root == null || root.isNull() || !root.isObject()) {
      throw new IncidentValidationException("PATCH body must be a JSON object");
    }
    Iterator<String> names = root.fieldNames();
    while (names.hasNext()) {
      String name = names.next();
      if (!ALLOWED_KEYS.contains(name)) {
        throw new IncidentValidationException("unknown field: " + name);
      }
    }
    boolean hasUpdatableKey = root.has("title") || root.has("description") || root.has("severity");
    if (!hasUpdatableKey) {
      throw new IncidentValidationException("patch must contain at least one of title, description, severity");
    }

    var title = java.util.Optional.<String>empty();
    if (root.has("title")) {
      JsonNode n = root.get("title");
      if (n.isNull() || !n.isTextual()) {
        throw new IncidentValidationException("title must be a non-null string");
      }
      title = java.util.Optional.of(n.asText());
    }

    var description = java.util.Optional.<String>empty();
    if (root.has("description")) {
      JsonNode n = root.get("description");
      if (n.isNull()) {
        throw new IncidentValidationException(
            "description must not be null; omit the key to leave unchanged, or use an empty string to clear");
      }
      if (!n.isTextual()) {
        throw new IncidentValidationException("description must be a string");
      }
      description = java.util.Optional.of(n.asText());
    }

    var severity = java.util.Optional.<IncidentSeverity>empty();
    if (root.has("severity")) {
      JsonNode n = root.get("severity");
      if (n.isNull() || !n.isTextual()) {
        throw new IncidentValidationException("severity must be a non-null string");
      }
      try {
        severity = java.util.Optional.of(IncidentSeverity.valueOf(n.asText()));
      } catch (IllegalArgumentException ex) {
        throw new IncidentValidationException("severity must be one of SEV1, SEV2, SEV3, SEV4");
      }
    }

    return new IncidentFieldPatch(title, description, severity);
  }
}
