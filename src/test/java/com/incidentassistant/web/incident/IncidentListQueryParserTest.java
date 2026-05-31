package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.IncidentValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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
  void parsesBlankParametersAsDefaults() {
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse("  ", "  ", "  ", "  ");
    assertThat(q.page()).isEqualTo(0);
    assertThat(q.size()).isEqualTo(20);
    assertThat(q.statusFilter()).isEmpty();
    assertThat(q.sortCreatedAtAscending()).isFalse();
  }

  @Test
  void parsesExplicitPageAndSize() {
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse("2", "50", null, null);
    assertThat(q.page()).isEqualTo(2);
    assertThat(q.size()).isEqualTo(50);
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
  void rejectsSizeZero() {
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, "0", null, null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("size");
  }

  @Test
  void parsesStatusFilterAndDetectsDuplicates() {
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse(null, null, "DRAFT, OPEN", null);
    assertThat(q.statusFilter()).containsExactly(IncidentStatus.DRAFT, IncidentStatus.OPEN);
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, null, "DRAFT,DRAFT", null))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void rejectsNonIntegerPageAndSize() {
    assertThatThrownBy(() -> IncidentListQueryParser.parse("x", null, null, null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("page");
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, "x", null, null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("size");
  }

  @Test
  void rejectsInvalidStatusTokens() {
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, null, "DRAFT,,OPEN", null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("empty");
    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, null, "NOPE", null))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("unknown status");
  }

  @Test
  void parsesExplicitSortAndRejectsInvalidSort() {
    IncidentListQueryParser.ParsedIncidentListQuery asc =
        IncidentListQueryParser.parse(null, null, null, "createdAt,asc");
    assertThat(asc.sortCreatedAtAscending()).isTrue();

    IncidentListQueryParser.ParsedIncidentListQuery desc =
        IncidentListQueryParser.parse(null, null, null, "createdAt,desc");
    assertThat(desc.sortCreatedAtAscending()).isFalse();

    assertThatThrownBy(() -> IncidentListQueryParser.parse(null, null, null, "bad"))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("sort");
  }

  @Test
  void assertOnlyKnownParameters_rejectsUnknown() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameters(Map.of("page", "0", "foo", "1"));

    assertThatThrownBy(() -> IncidentListQueryParser.assertOnlyKnownParameters(request))
        .isInstanceOf(IncidentValidationException.class)
        .hasMessageContaining("unknown query parameter");
  }
}
