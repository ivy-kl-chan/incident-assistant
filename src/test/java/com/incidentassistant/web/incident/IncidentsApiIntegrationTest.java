package com.incidentassistant.web.incident;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class IncidentsApiIntegrationTest extends PostgresIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

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
        .andExpect(header().string(HttpHeaders.ETAG, "\"" + root.get("version").asLong() + "\""))
        .andExpect(jsonPath("$.title").value("Outage"))
        .andExpect(jsonPath("$.description").value("down"))
        .andExpect(jsonPath("$.transitionReason").doesNotExist());
  }

  @Test
  void patch_withValidIfMatch_succeedsAndAdvancesEtag() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"P\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(createdJson.get("id").asText());

    MvcResult got = mockMvc.perform(get("/api/v1/incidents/" + id)).andExpect(status().isOk()).andReturn();
    String etag1 = got.getResponse().getHeader(HttpHeaders.ETAG);

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, etag1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Patched\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Patched"))
        .andExpect(jsonPath("$.version").value(2))
        .andExpect(header().string(HttpHeaders.ETAG, "\"2\""));
  }

  @Test
  void patch_staleIfMatch_returns412() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"S\",\"severity\":\"SEV3\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(createdJson.get("id").asText());
    String etag1 = createdJson.get("version").asText();
    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"" + etag1 + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"A\"}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"" + etag1 + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"B\"}"))
        .andExpect(status().isPreconditionFailed())
        .andExpect(jsonPath("$.detail").exists());
  }

  @Test
  void patch_missingIfMatch_returns412() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"M\",\"severity\":\"SEV2\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"X\"}"))
        .andExpect(status().isPreconditionFailed());
  }

  @Test
  void patch_ifMatchStar_returns412() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Star\",\"severity\":\"SEV2\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "*")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"X\"}"))
        .andExpect(status().isPreconditionFailed());
  }

  @Test
  void patch_onClosed_returns409() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"ClosedRow\",\"severity\":\"SEV1\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode j = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(j.get("id").asText());
    String etag = "\"" + j.get("version").asText() + "\"";

    jdbcTemplate.update("UPDATE incidents SET status = 'CLOSED' WHERE id = ?", id);

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, etag)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Nope\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void patch_emptyObject_returns400() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"E\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(createdJson.get("id").asText());
    String etag = createdJson.get("version").asText();

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"" + etag + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patch_wrongIfMatch_returns412() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"W\",\"severity\":\"SEV3\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"99\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"X\"}"))
        .andExpect(status().isPreconditionFailed())
        .andExpect(jsonPath("$.detail").exists());
  }

  @Test
  void patch_malformedJson_returns400() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"J\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"1\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patch_unknownField_returns400() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"U\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"1\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foo\":\"bar\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("unknown field")));
  }

  @Test
  void patch_nonObjectJson_returns400() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"A\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"1\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patch_onCancelled_returns409() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"CancelledRow\",\"severity\":\"SEV2\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode j = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(j.get("id").asText());
    String etag = "\"" + j.get("version").asText() + "\"";

    jdbcTemplate.update("UPDATE incidents SET status = 'CANCELLED' WHERE id = ?", id);

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, etag)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Nope\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void patch_onOpen_succeeds() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"OpenRow\",\"severity\":\"SEV3\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode j = objectMapper.readTree(created.getResponse().getContentAsString());
    UUID id = UUID.fromString(j.get("id").asText());
    String etag = "\"" + j.get("version").asText() + "\"";

    jdbcTemplate.update("UPDATE incidents SET status = 'OPEN' WHERE id = ?", id);

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, etag)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"OpenPatched\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("OpenPatched"))
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.version").value(2));
  }

  @Test
  void patch_wrongContentType_returns415() throws Exception {
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Ct\",\"severity\":\"SEV4\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    UUID id =
        UUID.fromString(
            objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"1\"")
                .contentType(MediaType.TEXT_PLAIN)
                .content("{\"title\":\"x\"}"))
        .andExpect(status().isUnsupportedMediaType());
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
