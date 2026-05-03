package com.incidentassistant.web.incident;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentassistant.testsupport.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class IncidentsApiIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void postThenGet_roundTrip_1aFields() throws Exception {
    String body =
        """
        {"title":"Outage","description":"down","severity":"SEV2"}
        """;

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.source").value("MANUAL"))
            .andExpect(jsonPath("$.severity").value("SEV2"))
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andReturn();

    JsonNode root = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(root.get("id").asText());

    mockMvc
        .perform(get("/api/v1/incidents/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Outage"))
        .andExpect(jsonPath("$.description").value("down"))
        .andExpect(jsonPath("$.transitionReason").doesNotExist());
  }

  @Test
  void getUnknown_returns404() throws Exception {
    UUID id = UUID.fromString("00000000-0000-0000-0000-000000000099");
    mockMvc.perform(get("/api/v1/incidents/" + id)).andExpect(status().isNotFound());
  }

  @Test
  void list_paginationAndSort() throws Exception {
    MvcResult baseline =
        mockMvc.perform(get("/api/v1/incidents").param("size", "1")).andReturn();
    long initialTotal =
        objectMapper
            .readTree(baseline.getResponse().getContentAsString())
            .get("totalElements")
            .asLong();

    mockMvc
        .perform(
            post("/api/v1/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"A\",\"severity\":\"SEV4\"}"))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"B\",\"severity\":\"SEV3\"}"))
        .andExpect(status().isCreated());

    long expectedTotal = initialTotal + 2;
    int expectedPages = (int) Math.ceil(expectedTotal / 1.0);

    mockMvc
        .perform(get("/api/v1/incidents").param("size", "1").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.totalElements").value((int) expectedTotal))
        .andExpect(jsonPath("$.totalPages").value(expectedPages));

    mockMvc
        .perform(
            get("/api/v1/incidents")
                .param("status", "DRAFT")
                .param("sort", "createdAt,asc")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)))
        .andExpect(jsonPath("$.items[0].status").value("DRAFT"));
  }

  @Test
  void list_badPage_returns400() throws Exception {
    mockMvc
        .perform(get("/api/v1/incidents").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").exists());
  }

  @Test
  void list_unknownQueryKey_source_returns400() throws Exception {
    mockMvc
        .perform(get("/api/v1/incidents").param("source", "MANUAL"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("unknown query parameter: source"));
  }

  @Test
  void requestId_echoedWhenProvided() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .header(RequestIdFilter.REQUEST_ID_HEADER, "trace-abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"R\",\"severity\":\"SEV1\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, "trace-abc"));
  }

  @Test
  void post_wrongContentType_returns415() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .contentType(MediaType.TEXT_PLAIN)
                .content("{\"title\":\"x\",\"severity\":\"SEV1\"}"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void post_missingTitle_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"severity\":\"SEV1\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("title is required"));
  }

  @Test
  void post_missingSeverity_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Only title\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("severity is required"));
  }
}
