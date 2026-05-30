package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentassistant.testsupport.PostgresIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Parallel PATCH against a real Tomcat listener (MockMvc is not thread-safe). Two writers with the
 * same {@code If-Match}: exactly one {@code 200}, one {@code 412}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IncidentsPatchConcurrencyIntegrationTest extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void parallelPatch_sameIfMatch_oneSucceedsOnePreconditionFailed() throws Exception {
    ResponseEntity<String> created =
        restTemplate.postForEntity(
            "/api/v1/incidents",
            jsonEntity("{\"title\":\"Race\",\"severity\":\"SEV4\"}"),
            String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    JsonNode createdJson = objectMapper.readTree(created.getBody());
    UUID id = UUID.fromString(createdJson.get("id").asText());
    String etag = "\"" + createdJson.get("version").asText() + "\"";

    String patchUrl = "/api/v1/incidents/" + id;
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);

    try {
      List<Future<Integer>> futures = new ArrayList<>();
      for (int writer = 0; writer < 2; writer++) {
        final int n = writer;
        futures.add(
            pool.submit(
                () -> {
                  ready.countDown();
                  start.await();
                  HttpHeaders headers = new HttpHeaders();
                  headers.set(HttpHeaders.IF_MATCH, etag);
                  headers.setContentType(MediaType.APPLICATION_JSON);
                  HttpEntity<String> body =
                      new HttpEntity<>("{\"title\":\"Writer" + n + "\"}", headers);
                  ResponseEntity<String> response =
                      restTemplate.exchange(patchUrl, HttpMethod.PATCH, body, String.class);
                  return response.getStatusCode().value();
                }));
      }

      assertThat(ready.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
      start.countDown();

      List<Integer> statuses = new ArrayList<>();
      for (Future<Integer> f : futures) {
        statuses.add(f.get(30, java.util.concurrent.TimeUnit.SECONDS));
      }

      assertThat(statuses).containsExactlyInAnyOrder(200, 412);
    } finally {
      pool.shutdownNow();
    }
  }

  private static HttpEntity<String> jsonEntity(String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(json, headers);
  }
}
