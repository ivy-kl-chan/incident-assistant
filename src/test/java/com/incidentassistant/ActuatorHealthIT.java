package com.incidentassistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthIT {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void actuatorHealthReturnsOk() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"status\":\"UP\"");
  }

  @Test
  void actuatorHealthReadinessReturnsOkAndUpBeforeDatabaseWiring() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/health/readiness", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"status\":\"UP\"");
  }

  @Test
  void nonHealthActuatorEndpointsAreNotExposed() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/actuator/env", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
