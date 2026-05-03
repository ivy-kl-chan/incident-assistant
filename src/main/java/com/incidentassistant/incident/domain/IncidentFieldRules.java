package com.incidentassistant.incident.domain;

import java.util.Objects;

/** Field constraints aligned with {@code phase-1a-monolith-core/api-contract.md} (1a). */
public final class IncidentFieldRules {

  public static final int TITLE_MIN_LEN = 1;
  public static final int TITLE_MAX_LEN = 200;
  public static final int DESCRIPTION_MAX_LEN = 32_768;

  private IncidentFieldRules() {}

  public static String requireValidTitle(String rawTitle) {
    if (rawTitle == null) {
      throw new IncidentValidationException("title is required");
    }
    String trimmed = rawTitle.trim();
    if (trimmed.length() < TITLE_MIN_LEN || trimmed.length() > TITLE_MAX_LEN) {
      throw new IncidentValidationException(
          "title must be 1–200 characters after trim");
    }
    return trimmed;
  }

  public static String normalizeDescription(String description) {
    if (description == null) {
      return null;
    }
    if (description.length() > DESCRIPTION_MAX_LEN) {
      throw new IncidentValidationException(
          "description must be at most " + DESCRIPTION_MAX_LEN + " characters");
    }
    return description;
  }

  public static IncidentSeverity requireValidSeverity(IncidentSeverity severity) {
    if (severity == null) {
      throw new IncidentValidationException("severity is required");
    }
    return severity;
  }

  /**
   * Fields {@code title}, {@code description}, {@code severity} may be edited only while status
   * is {@link IncidentStatus#DRAFT} or {@link IncidentStatus#OPEN} (1a API contract).
   */
  public static void requireEditableState(IncidentStatus status) {
    Objects.requireNonNull(status, "status");
    if (status != IncidentStatus.DRAFT && status != IncidentStatus.OPEN) {
      throw new IllegalIncidentTransitionException(
          "incident fields cannot be edited in status " + status);
    }
  }
}
