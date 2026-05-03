package com.incidentassistant;

import static org.assertj.core.api.Assertions.assertThat;

import com.incidentassistant.incident.application.ManualIncidentService;
import com.incidentassistant.incident.domain.IncidentSeverity;
import com.incidentassistant.incident.domain.IncidentStatus;
import com.incidentassistant.support.PostgresSpringBootIntegrationTest;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

class ManualIncidentPersistenceIntegrationTest extends PostgresSpringBootIntegrationTest {

  @Autowired private ManualIncidentService manualIncidents;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void roundTrip_insertManualDraft_oneBColumnsRemainNull() {
    var created =
        manualIncidents.createDraft(
            "CPU saturation", "Queue depth elevated", IncidentSeverity.SEV2);

    assertThat(created.getStatus()).isEqualTo(IncidentStatus.DRAFT);

    String rule =
        jdbcTemplate.queryForObject(
            "SELECT created_by_rule_id FROM incidents WHERE id = ?",
            String.class,
            created.getId());
    String fp =
        jdbcTemplate.queryForObject(
            "SELECT signal_fingerprint FROM incidents WHERE id = ?",
            String.class,
            created.getId());
    Object telemetry =
        jdbcTemplate.queryForObject(
            "SELECT telemetry_context FROM incidents WHERE id = ?",
            Object.class,
            created.getId());

    assertThat(rule).isNull();
    assertThat(fp).isNull();
    assertThat(telemetry).isNull();

    String source =
        jdbcTemplate.queryForObject(
            "SELECT source FROM incidents WHERE id = ?", String.class, created.getId());
    assertThat(source).isEqualTo("MANUAL");
  }

  @Test
  void version_incrementsAfterSuccessfulUpdate() {
    var created =
        manualIncidents.createDraft("title", null, IncidentSeverity.SEV3);
    assertThat(created.getVersion()).isZero();

    var promoted =
        manualIncidents.transition(created.getId(), 0L, IncidentStatus.OPEN);
    assertThat(promoted.getVersion()).isEqualTo(1L);
    assertThat(promoted.getStatus()).isEqualTo(IncidentStatus.OPEN);
  }

  /**
   * Synthetic SIGNAL row (would only be written by Phase 1b); proves manual service refuses to
   * mutate it.
   */
  @Test
  @Sql(statements = {
        "INSERT INTO incidents (id, version, status, title, description, severity, source, created_at, updated_at)"
            + " VALUES ('11111111-1111-1111-1111-111111111111', 0, 'DRAFT', 'sig', null, 'SEV4', 'SIGNAL', now(), now())"
      })
  void manualService_rejectsMutationWhenSourceNotManual() {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    Assertions.assertThatThrownBy(() -> manualIncidents.transition(id, 0L, IncidentStatus.OPEN))
        .isInstanceOf(IllegalStateException.class);
  }
}
