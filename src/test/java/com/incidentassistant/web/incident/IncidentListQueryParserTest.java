package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.IncidentValidationException;
import org.junit.jupiter.api.Test;

class IncidentListQueryParserTest {

  @Test
  void parsesDefaults() {
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse(null, null, null, null);
    assertThat(q.page()).isEqualTo(0);
    assertThat(q.size()).isEqualTo(20);
    assertThat(q.statusFilter()).isEmpty();
    assertThat(q.sortCreatedAtAscending()).isFalse();
  }

  @Test
  void rejectsNegativePage() {
    assertThatThrownBy(() -> IncidentListQueryParser.parse("-1", null, null, null))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void rejectsSizeAboveMax() {
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, "101", null, null))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void parsesStatusFilterAndDetectsDuplicates() {
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse(null, null, "DRAFT, OPEN", null);
    assertThat(q.statusFilter()).containsExactly(IncidentStatus.DRAFT, IncidentStatus.OPEN);
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, null, "DRAFT,DRAFT", null))
        .isInstanceOf(IncidentValidationException.class);
  }
}
