package com.incidentassistant.web.incident;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.incidentassistant.application.incident.ManualIncidentService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
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
}
