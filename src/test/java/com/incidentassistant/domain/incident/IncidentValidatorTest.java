package com.incidentassistant.domain.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IncidentValidatorTest {

  @Test
  void validateTitle_acceptsTrimmedLengthInRange() {
    String t = "a".repeat(200);
    assertThat(IncidentValidator.validateTitle("  " + t + "  ")).isEqualTo(t);
  }

  @Test
  void validateTitle_rejectsTooShortAfterTrim() {
    assertThatThrownBy(() -> IncidentValidator.validateTitle("   "))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("title");
  }

  @Test
  void validateTitle_rejectsTooLongAfterTrim() {
    String t = "a".repeat(201);
    assertThatThrownBy(() -> IncidentValidator.validateTitle(t))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("title");
  }

  @Test
  void validateDescription_rejectsTooLongAfterTrim() {
    String d = "x".repeat(32_769);
    assertThatThrownBy(() -> IncidentValidator.validateDescription(d))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("description");
  }

  @Test
  void validateDescription_acceptsNullAndMaxLength() {
    assertThat(IncidentValidator.validateDescription(null)).isNull();
    String d = "y".repeat(32_768);
    assertThat(IncidentValidator.validateDescription(d)).hasSize(32_768);
  }

  @Test
  void validateCreate_validatesAllFields() {
    CreateManualIncidentCommand cmd =
        new CreateManualIncidentCommand("ok", null, IncidentSeverity.SEV2);
    CreateManualIncidentCommand out = IncidentValidator.validateCreate(cmd);
    assertThat(out.title()).isEqualTo("ok");
    assertThat(out.description()).isNull();
    assertThat(out.severity()).isEqualTo(IncidentSeverity.SEV2);
  }

  @Test
  void validatePatch_validatesPresentFieldsOnly() {
    IncidentFieldPatch patch =
        new IncidentFieldPatch(Optional.empty(), Optional.of("hi"), Optional.empty());
    IncidentFieldPatch out = IncidentValidator.validatePatch(patch);
    assertThat(out.description()).contains("hi");
  }

  @Test
  void validateSeverity_requiresNonNull() {
    assertThatThrownBy(() -> IncidentValidator.validateSeverity(null))
        .isInstanceOf(NullPointerException.class);
  }
}
