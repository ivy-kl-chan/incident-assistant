package com.incidentassistant.domain.incident;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates incident field constraints aligned with {@code api-contract.md} (POST/PATCH).
 */
public final class IncidentValidator {

  static final int TITLE_MIN_LEN = 1;
  static final int TITLE_MAX_LEN = 200;
  static final int DESCRIPTION_MAX_LEN = 32_768;

  private IncidentValidator() {}

  public static String validateTitle(String rawTitle) {
    Objects.requireNonNull(rawTitle, "title");
    String trimmed = rawTitle.trim();
    if (trimmed.length() < TITLE_MIN_LEN || trimmed.length() > TITLE_MAX_LEN) {
      throw new IncidentValidationException(
          "title must be " + TITLE_MIN_LEN + "–" + TITLE_MAX_LEN + " characters after trim");
    }
    return trimmed;
  }

  /**
   * Description may be null (absent). When non-null, length after trim must not exceed {@value
   * #DESCRIPTION_MAX_LEN}.
   */
  public static String validateDescription(String rawDescription) {
    if (rawDescription == null) {
      return null;
    }
    String trimmed = rawDescription.trim();
    if (trimmed.length() > DESCRIPTION_MAX_LEN) {
      throw new IncidentValidationException(
          "description must be at most " + DESCRIPTION_MAX_LEN + " characters after trim");
    }
    return trimmed.isEmpty() ? null : trimmed;
  }

  public static IncidentSeverity validateSeverity(IncidentSeverity severity) {
    Objects.requireNonNull(severity, "severity");
    return severity;
  }

  /**
   * Validates fields for a create command (POST semantics).
   */
  public static CreateManualIncidentCommand validateCreate(CreateManualIncidentCommand command) {
    String title = validateTitle(command.title());
    String description = validateDescription(command.description());
    IncidentSeverity severity = validateSeverity(command.severity());
    return new CreateManualIncidentCommand(title, description, severity);
  }

  /**
   * Validates only patch fields that are present; unchanged fields are not validated here.
   */
  public static IncidentFieldPatch validatePatch(IncidentFieldPatch patch) {
    Optional<String> title = patch.title().map(IncidentValidator::validateTitle);
    Optional<String> description = patch.description().map(IncidentValidator::validateDescription);
    Optional<IncidentSeverity> severity =
        patch.severity().map(IncidentValidator::validateSeverity);
    return new IncidentFieldPatch(title, description, severity);
  }
}
