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

  @Test
  void nullRoot_throws() {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("JSON object");
  }

  @Test
  void jsonNull_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.nullNode()))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("JSON object");
  }

  @Test
  void invalidTitle_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"title\":null}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("title");
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"title\":1}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("title");
  }

  @Test
  void description_parsedAndValidated() throws Exception {
    IncidentFieldPatch cleared =
        PatchIncidentBodyParser.parse(mapper.readTree("{\"description\":\"\"}"));
    assertThat(cleared.description()).contains("");

    IncidentFieldPatch withText =
        PatchIncidentBodyParser.parse(mapper.readTree("{\"description\":\"note\"}"));
    assertThat(withText.description()).contains("note");

    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"description\":null}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("description");
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"description\":1}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("description");
  }

  @Test
  void invalidSeverity_throws() throws Exception {
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"severity\":null}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("severity");
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"severity\":1}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("severity");
    assertThatThrownBy(() -> PatchIncidentBodyParser.parse(mapper.readTree("{\"severity\":\"BAD\"}")))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("SEV1");
  }
}
