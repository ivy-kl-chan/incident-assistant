package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentassistant.domain.incident.IncidentFieldPatch;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentValidationException;
import org.junit.jupiter.api.Test;

class PatchIncidentBodyParserTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void parsesTitleOnly() throws Exception {
    IncidentFieldPatch p = PatchIncidentBodyParser.parse(mapper.readTree("{\"title\":\"  x  \"}"));
    assertThat(p.title()).contains("  x  ");
    assertThat(p.description()).isEmpty();
    assertThat(p.severity()).isEmpty();
  }

  @Test
  void emptyObject_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("at least one");
  }

  @Test
  void unknownField_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"foo\":1}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("unknown field");
  }

  @Test
  void nonObject_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("[]")))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void severityEnum_parsed() throws Exception {
    IncidentFieldPatch p = PatchIncidentBodyParser.parse(mapper.readTree("{\"severity\":\"SEV2\"}"));
    assertThat(p.severity()).contains(IncidentSeverity.SEV2);
  }
}
