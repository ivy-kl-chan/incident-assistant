package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.incidentassistant.domain.incident.IncidentStaleVersionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void preconditionFailed_usesDefaultDetailWhenMessageNull() {
    var response = handler.preconditionFailed(new IncidentStaleVersionException(null));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
    assertThat(response.getBody()).containsEntry("detail", "Precondition failed");
  }

  @Test
  void preconditionFailed_usesExceptionMessageWhenPresent() {
    var response = handler.preconditionFailed(new IncidentStaleVersionException("version mismatch"));

    assertThat(response.getBody()).containsEntry("detail", "version mismatch");
  }
}
