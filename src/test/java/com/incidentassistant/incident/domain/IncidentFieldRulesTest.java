package com.incidentassistant.incident.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class IncidentFieldRulesTest {

  @Test
  void title_trimmedAndValidated() {
    assertThat(IncidentFieldRules.requireValidTitle("  hello  ")).isEqualTo("hello");
  }

  @Test
  void title_emptyAfterTrimRejected() {
    assertThatThrownBy(() -> IncidentFieldRules.requireValidTitle("   "))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("title");
  }

  @Test
  void title_tooLongRejected() {
    assertThatThrownBy(() -> IncidentFieldRules.requireValidTitle("x".repeat(201)))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void description_tooLongRejected() {
    assertThatThrownBy(
            () -> IncidentFieldRules.normalizeDescription("x".repeat(32_769)))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void severity_nullRejected() {
    assertThatThrownBy(() -> IncidentFieldRules.requireValidSeverity(null))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void fields_notEditableInClosed() {
    assertThatThrownBy(() -> IncidentFieldRules.requireEditableState(IncidentStatus.CLOSED))
        .isInstanceOf(IllegalIncidentTransitionException.class);
  }
}
