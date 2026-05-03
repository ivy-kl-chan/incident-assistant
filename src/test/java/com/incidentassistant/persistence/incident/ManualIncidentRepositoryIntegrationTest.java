package com.incidentassistant.persistence.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.incidentassistant.application.incident.ManualIncidentService;
import com.incidentassistant.domain.incident.CreateManualIncidentCommand;
import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentFieldPatch;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.testsupport.PostgresIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ManualIncidentRepositoryIntegrationTest extends PostgresIntegrationTest {

  @Autowired private ManualIncidentService manualIncidentService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void roundTrip_insertManual_thenUpdate_advancesVersionAndUpdatedAt() throws Exception {
    Incident created =
        manualIncidentService.create(
            new CreateManualIncidentCommand("Hello", "body", IncidentSeverity.SEV3));

    assertThat(created.source()).isEqualTo(IncidentSource.MANUAL);
    assertThat(created.status()).isEqualTo(IncidentStatus.DRAFT);
    assertThat(created.version()).isEqualTo(1L);

    Optional<String> sourceFromDb =
        jdbcTemplate.query(
            "select source from incidents where id = ?",
            rs -> {
              if (!rs.next()) {
                return Optional.<String>empty();
              }
              return Optional.of(rs.getString(1));
            },
            created.id());
    assertThat(sourceFromDb).contains("MANUAL");

    String fingerprint =
        jdbcTemplate.queryForObject(
            "select signal_fingerprint from incidents where id = ?", String.class, created.id());
    assertThat(fingerprint).isNull();

    Thread.sleep(25);

    Incident updated =
        manualIncidentService.updateFields(
            created.id(),
            1L,
            new IncidentFieldPatch(Optional.of("Hello2"), Optional.empty(), Optional.empty()));

    assertThat(updated.version()).isEqualTo(2L);
    assertThat(updated.title()).isEqualTo("Hello2");
    assertThat(updated.updatedAt()).isAfter(created.updatedAt());
    assertThat(updated.createdAt()).isEqualTo(created.createdAt());
  }

}
