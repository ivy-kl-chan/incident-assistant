package com.incidentassistant.web.incident;

import com.incidentassistant.domain.incident.IncidentStaleVersionException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code If-Match} for Phase 1a incident mutations: strong quoted decimal entity-tags derived
 * from {@code version}. Wildcard {@code *} (including {@code W/*}) is rejected with {@link
 * IncidentStaleVersionException} (HTTP 412).
 */
public final class IfMatchPrecondition {

  private static final Pattern STRONG_DECIMAL_ETAG = Pattern.compile("\"(\\d+)\"");

  private IfMatchPrecondition() {}

  /**
   * Ensures {@code If-Match} is present, does not use unsupported wildcards, lists at least one
   * strong decimal tag, and includes {@code currentVersion}.
   */
  public static void requireCurrentVersionMatched(String ifMatchHeader, long currentVersion) {
    if (ifMatchHeader == null || ifMatchHeader.isBlank()) {
      throw new IncidentStaleVersionException("If-Match is required");
    }
    for (String raw : ifMatchHeader.split(",")) {
      String token = raw.trim();
      if (token.equals("*") || token.equalsIgnoreCase("W/*")) {
        throw new IncidentStaleVersionException("If-Match * is not supported");
      }
      if (token.regionMatches(true, 0, "W/", 0, 2)) {
        throw new IncidentStaleVersionException("weak If-Match entity-tags are not supported");
      }
    }
    Set<Long> versions = new LinkedHashSet<>();
    Matcher m = STRONG_DECIMAL_ETAG.matcher(ifMatchHeader);
    while (m.find()) {
      versions.add(Long.parseLong(m.group(1)));
    }
    if (versions.isEmpty()) {
      throw new IncidentStaleVersionException("If-Match must include a quoted version entity-tag");
    }
    if (!versions.contains(currentVersion)) {
      throw new IncidentStaleVersionException("If-Match does not match current resource version");
    }
  }
}
