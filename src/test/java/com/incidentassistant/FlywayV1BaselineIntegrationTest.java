package com.incidentassistant;

import static org.assertj.core.api.Assertions.assertThat;

import com.incidentassistant.testsupport.PostgresIntegrationTest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FlywayV1BaselineIntegrationTest extends PostgresIntegrationTest {

  @Autowired private DataSource dataSource;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void flywayAppliesExactlyV1OnEmptyDatabase() {
    Integer versionCount =
        jdbcTemplate.queryForObject(
            "select count(*) from flyway_schema_history where version = ?", Integer.class, "1");
    assertThat(versionCount).isEqualTo(1);

    Integer totalMigrations =
        jdbcTemplate.queryForObject("select count(*) from flyway_schema_history", Integer.class);
    assertThat(totalMigrations).isEqualTo(1);
  }

  @Test
  void v1TablesColumnsAndPartialIndexMatchDataModel() {
    assertThat(tableExists("incidents")).isTrue();
    assertThat(tableExists("signal_ingest_audit")).isTrue();
    assertThat(tableExists("signal_ingest_idempotency")).isTrue();

    assertThat(columnType("incidents", "telemetry_context")).isEqualTo("jsonb");
    assertThat(columnType("incidents", "source")).contains("character varying");

    assertThat(indexExists("idx_incidents_status_created_at")).isTrue();
    assertThat(partialFingerprintIndexExists()).isTrue();
  }

  private boolean tableExists(String table) {
    Integer n =
        jdbcTemplate.queryForObject(
            """
            select count(*) from information_schema.tables
            where table_schema = 'public' and table_name = ?
            """,
            Integer.class,
            table);
    return n != null && n == 1;
  }

  private String columnType(String table, String column) {
    return jdbcTemplate.queryForObject(
        """
        select data_type from information_schema.columns
        where table_schema = 'public' and table_name = ? and column_name = ?
        """,
        String.class,
        table,
        column);
  }

  private boolean indexExists(String indexName) {
    Integer n =
        jdbcTemplate.queryForObject(
            """
            select count(*) from pg_indexes
            where schemaname = 'public' and indexname = ?
            """,
            Integer.class,
            indexName);
    return n != null && n == 1;
  }

  private boolean partialFingerprintIndexExists() {
    String def =
        jdbcTemplate.query(
            """
            select indexdef from pg_indexes
            where schemaname = 'public'
              and tablename = 'incidents'
              and indexdef ilike '%signal_fingerprint%'
              and indexdef ilike '%WHERE%'
            """,
            rs -> {
              if (!rs.next()) {
                return null;
              }
              return rs.getString(1);
            });
    return def != null
        && def.contains("signal_fingerprint")
        && def.toLowerCase().contains("where");
  }

  @Test
  void incidentsDefaultSourceIsManual() throws Exception {
    try (Connection c = dataSource.getConnection();
        Statement st = c.createStatement();
        ResultSet rs =
            st.executeQuery(
                """
                select column_default from information_schema.columns
                where table_schema = 'public'
                  and table_name = 'incidents'
                  and column_name = 'source'
                """)) {
      assertThat(rs.next()).isTrue();
      String def = rs.getString(1);
      assertThat(def).isNotNull();
      assertThat(def).containsIgnoringCase("MANUAL");
    }
  }
}
