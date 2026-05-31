package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incidentassistant.domain.incident.IncidentStaleVersionException;
import org.junit.jupiter.api.Test;

class IfMatchPreconditionTest {

  @Test
  void acceptsQuotedVersionMatchingCurrent() {
    assertThatCode(() -> IfMatchPrecondition.requireCurrentVersionMatched("\"7\"", 7L)).doesNotThrowAnyException();
  }

  @Test
  void acceptsListWhenCurrentMatchesOneToken() {
    assertThatCode(() -> IfMatchPrecondition.requireCurrentVersionMatched("\"6\", \"7\"", 7L))
        .doesNotThrowAnyException();
  }

  @Test
  void missingHeader_throws412Family() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched(null, 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("If-Match is required");
  }

  @Test
  void blankHeader_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("   ", 1L))
        .isInstanceOf(IncidentStaleVersionException.class);
  }

  @Test
  void wildcardStar_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("*", 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("not supported");
  }

  @Test
  void wildcardWStar_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("W/*", 1L))
        .isInstanceOf(IncidentStaleVersionException.class);
  }

  @Test
  void wildcardInList_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("\"1\", *", 1L))
        .isInstanceOf(IncidentStaleVersionException.class);
  }

  @Test
  void wrongVersion_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("\"2\"", 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("does not match");
  }

  @Test
  void garbageWithoutQuotedNumber_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("opaque", 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("quoted version");
  }

  @Test
  void unquotedDecimal_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("1", 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("quoted version");
  }

  @Test
  void weakEtagPrefix_throws() {
    assertThatThrownBy(() -> IfMatchPrecondition.requireCurrentVersionMatched("W/\"1\"", 1L))
        .isInstanceOf(IncidentStaleVersionException.class)
        .hasMessageContaining("weak");
  }
}
