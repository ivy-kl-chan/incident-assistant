package com.incidentassistant.web.incident;

import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.IncidentValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Parses and validates list query parameters per Phase 1a {@code api-contract.md}. */
public final class IncidentListQueryParser {

  static final int DEFAULT_PAGE = 0;
  static final int DEFAULT_SIZE = 20;
  static final int MAX_SIZE = 100;
  static final String DEFAULT_SORT = "createdAt,desc";

  private static final Set<String> ALLOWED_KEYS = Set.of("page", "size", "status", "sort");

  private IncidentListQueryParser() {}

  public static void assertOnlyKnownParameters(HttpServletRequest request) {
    for (String name : request.getParameterMap().keySet()) {
      if (!ALLOWED_KEYS.contains(name)) {
        throw new IncidentValidationException("unknown query parameter: " + name);
      }
    }
  }

  public static ParsedIncidentListQuery parse(
      String pageRaw, String sizeRaw, String statusRaw, String sortRaw) {
    int page = parseNonNegativeInt(pageRaw, DEFAULT_PAGE, "page");
    int size = parsePositiveBoundedInt(sizeRaw, DEFAULT_SIZE, MAX_SIZE, "size");
    List<IncidentStatus> statusFilter = parseStatusFilter(statusRaw);
    boolean asc = parseSort(sortRaw);
    return new ParsedIncidentListQuery(page, size, statusFilter, asc);
  }

  private static int parseNonNegativeInt(String raw, int defaultValue, String field) {
    if (raw == null || raw.isBlank()) {
      return defaultValue;
    }
    try {
      int v = Integer.parseInt(raw.trim());
      if (v < 0) {
        throw new IncidentValidationException(field + " must be >= 0");
      }
      return v;
    } catch (NumberFormatException e) {
      throw new IncidentValidationException(field + " must be an integer");
    }
  }

  private static int parsePositiveBoundedInt(String raw, int defaultValue, int max, String field) {
    if (raw == null || raw.isBlank()) {
      return defaultValue;
    }
    try {
      int v = Integer.parseInt(raw.trim());
      if (v < 1 || v > max) {
        throw new IncidentValidationException(field + " must be between 1 and " + max);
      }
      return v;
    } catch (NumberFormatException e) {
      throw new IncidentValidationException(field + " must be an integer");
    }
  }

  private static List<IncidentStatus> parseStatusFilter(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    String[] parts = raw.split(",");
    List<IncidentStatus> ordered = new ArrayList<>();
    Set<IncidentStatus> seen = new HashSet<>();
    for (String part : parts) {
      String token = part.trim();
      if (token.isEmpty()) {
        throw new IncidentValidationException("status tokens must not be empty");
      }
      IncidentStatus status;
      try {
        status = IncidentStatus.valueOf(token.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        throw new IncidentValidationException("unknown status: " + token);
      }
      if (!seen.add(status)) {
        throw new IncidentValidationException("duplicate status in filter: " + status.name());
      }
      ordered.add(status);
    }
    return ordered;
  }

  /**
   * @return {@code true} for {@code createdAt,asc}, {@code false} for {@code createdAt,desc}
   */
  private static boolean parseSort(String raw) {
    String value = raw == null || raw.isBlank() ? DEFAULT_SORT : raw.trim();
    if ("createdAt,asc".equals(value)) {
      return true;
    }
    if ("createdAt,desc".equals(value)) {
      return false;
    }
    throw new IncidentValidationException(
        "sort must be createdAt,asc or createdAt,desc (default: createdAt,desc)");
  }

  public record ParsedIncidentListQuery(
      int page, int size, List<IncidentStatus> statusFilter, boolean sortCreatedAtAscending) {}
}
