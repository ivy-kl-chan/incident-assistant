package com.incidentassistant.incident.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class IncidentTransitionPolicyTest {

  @Test
  void draftToOpen() {
    assertThatCode(
            () ->
                IncidentTransitionPolicy.requireValidTransition(
                    IncidentStatus.DRAFT, IncidentStatus.OPEN))
        .doesNotThrowAnyException();
  }

  @Test
  void openToClosed() {
    assertThatCode(
            () ->
                IncidentTransitionPolicy.requireValidTransition(
                    IncidentStatus.OPEN, IncidentStatus.CLOSED))
        .doesNotThrowAnyException();
  }

  @Test
  void closedToOpen_rejected() {
    assertThatThrownBy(
            () ->
                IncidentTransitionPolicy.requireValidTransition(
                    IncidentStatus.CLOSED, IncidentStatus.OPEN))
        .isInstanceOf(IllegalIncidentTransitionException.class);
  }

  @Test
  void draftToClosed_rejected() {
    assertThatThrownBy(
            () ->
                IncidentTransitionPolicy.requireValidTransition(
                    IncidentStatus.DRAFT, IncidentStatus.CLOSED))
        .isInstanceOf(IllegalIncidentTransitionException.class);
  }
}
