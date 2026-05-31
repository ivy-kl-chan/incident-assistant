package com.incidentassistant.domain.incident;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class IncidentPageTest {

  @Test
  void totalPages_computesCeilingDivision() {
    assertThat(new IncidentPage(List.of(), 0, 0, 20).totalPages()).isZero();
    assertThat(new IncidentPage(List.of(), 41, 0, 20).totalPages()).isEqualTo(3);
  }

  @Test
  void totalPages_returnsZeroWhenSizeNonPositive() {
    assertThat(new IncidentPage(List.of(), 10, 0, 0).totalPages()).isZero();
    assertThat(new IncidentPage(List.of(), 10, 0, -1).totalPages()).isZero();
  }
}
