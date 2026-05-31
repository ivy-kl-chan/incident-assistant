package com.incidentassistant.persistence.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class JdbcManualIncidentRepositoryTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @Test
  void insert_rejectsNonManualSource() {
    JdbcManualIncidentRepository repository = new JdbcManualIncidentRepository(jdbcTemplate);
    Instant t = Instant.parse("2026-05-01T12:00:00Z");
    Incident signal =
        new Incident(
            UUID.randomUUID(),
            1L,
            IncidentStatus.DRAFT,
            "t",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.SIGNAL,
            t,
            t);

    assertThatThrownBy(() -> repository.insert(signal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MANUAL");
  }

  @Test
  @SuppressWarnings("unchecked")
  void findManualIncidentsPage_treatsNullCountAsZero() {
    JdbcManualIncidentRepository repository = new JdbcManualIncidentRepository(jdbcTemplate);
    when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
        .thenReturn(null);
    when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
        .thenReturn(List.of());

    var page = repository.findManualIncidentsPage(0, 20, List.of(), false);

    assertThat(page.totalElements()).isZero();
    assertThat(page.items()).isEmpty();
  }
}
