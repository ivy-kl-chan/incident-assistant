package com.incidentassistant.web.incident;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.incidentassistant.application.incident.ManualIncidentService;
import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = IncidentController.class)
@Import(ApiExceptionHandler.class)
class IncidentControllerWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ManualIncidentService incidentService;

  @Test
  void getById_whenPersistenceFails_returns503() throws Exception {
    UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    when(incidentService.getForApi(any(UUID.class)))
        .thenThrow(new DataAccessResourceFailureException("simulated"));

    mockMvc.perform(get("/api/v1/incidents/" + id)).andExpect(status().isServiceUnavailable());
  }

  @Test
  void list_whenPersistenceFails_returns503() throws Exception {
    when(incidentService.list(anyInt(), anyInt(), anyList(), anyBoolean()))
        .thenThrow(new DataAccessResourceFailureException("simulated"));

    mockMvc.perform(get("/api/v1/incidents")).andExpect(status().isServiceUnavailable());
  }

  @Test
  void patch_missingIfMatch_returns412() throws Exception {
    UUID id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    when(incidentService.getForApi(id)).thenReturn(sampleIncident(id, 1L));

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"n\"}"))
        .andExpect(status().isPreconditionFailed());

    verify(incidentService).getForApi(id);
    verify(incidentService, never()).updateFields(any(), anyLong(), any());
  }

  @Test
  void patch_wrongIfMatch_returns412() throws Exception {
    UUID id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    when(incidentService.getForApi(id)).thenReturn(sampleIncident(id, 1L));

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"2\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"n\"}"))
        .andExpect(status().isPreconditionFailed());

    verify(incidentService, never()).updateFields(any(), anyLong(), any());
  }

  @Test
  void patch_ifMatchWildcard_returns412() throws Exception {
    UUID id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    when(incidentService.getForApi(id)).thenReturn(sampleIncident(id, 1L));

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "*")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"n\"}"))
        .andExpect(status().isPreconditionFailed());
  }

  @Test
  void patch_success_returns200WithEtag() throws Exception {
    UUID id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    Incident current = sampleIncident(id, 1L);
    Incident updated = sampleIncident(id, 2L);
    when(incidentService.getForApi(id)).thenReturn(current);
    when(incidentService.updateFields(eq(id), eq(1L), any())).thenReturn(updated);

    mockMvc
        .perform(
            patch("/api/v1/incidents/" + id)
                .header(HttpHeaders.IF_MATCH, "\"1\"")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"n\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ETAG, "\"2\""));
  }

  private static Incident sampleIncident(UUID id, long version) {
    Instant t = Instant.parse("2026-05-01T12:00:00Z");
    return new Incident(
        id,
        version,
        IncidentStatus.DRAFT,
        "t",
        null,
        IncidentSeverity.SEV1,
        IncidentSource.MANUAL,
        t,
        t);
  }
}
