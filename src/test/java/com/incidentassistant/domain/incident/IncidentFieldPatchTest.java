package com.incidentassistant.domain.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IncidentFieldPatchTest {

  @Test
  void isEmpty_reflectsPresentFields() {
    assertThat(IncidentFieldPatch.empty().isEmpty()).isTrue();
    assertThat(new IncidentFieldPatch(Optional.of("t"), Optional.empty(), Optional.empty()).isEmpty())
        .isFalse();
    assertThat(new IncidentFieldPatch(Optional.empty(), Optional.of("d"), Optional.empty()).isEmpty())
        .isFalse();
    assertThat(
            new IncidentFieldPatch(
                    Optional.empty(), Optional.empty(), Optional.of(IncidentSeverity.SEV3))
                .isEmpty())
        .isFalse();
  }
}
