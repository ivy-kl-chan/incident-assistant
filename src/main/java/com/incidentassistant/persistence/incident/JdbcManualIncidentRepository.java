package com.incidentassistant.persistence.incident;

import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.ManualIncidentRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * JDBC implementation of {@link ManualIncidentRepository}. Registered as a Spring bean from {@link
 * com.incidentassistant.autoconfigure.IncidentJdbcAutoConfiguration} after {@link JdbcTemplate}
 * exists.
 */
public class JdbcManualIncidentRepository implements ManualIncidentRepository {

  private static final RowMapper<Incident> ROW_MAPPER =
      (rs, rowNum) ->
          new Incident(
              rs.getObject("id", UUID.class),
              rs.getLong("version"),
              IncidentStatus.valueOf(rs.getString("status")),
              rs.getString("title"),
              rs.getString("description"),
              IncidentSeverity.valueOf(rs.getString("severity")),
              IncidentSource.valueOf(rs.getString("source")),
              rs.getTimestamp("created_at").toInstant(),
              rs.getTimestamp("updated_at").toInstant());

  private final JdbcTemplate jdbcTemplate;

  public JdbcManualIncidentRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Incident insert(Incident incident) {
    if (incident.source() != IncidentSource.MANUAL) {
      throw new IllegalArgumentException("Manual repository only persists MANUAL incidents");
    }
    jdbcTemplate.update(
        """
        INSERT INTO incidents (
          id, version, status, title, description, severity, source, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        incident.id(),
        incident.version(),
        incident.status().name(),
        incident.title(),
        incident.description(),
        incident.severity().name(),
        IncidentSource.MANUAL.name(),
        Timestamp.from(incident.createdAt()),
        Timestamp.from(incident.updatedAt()));
    return incident;
  }

  @Override
  public Optional<Incident> findById(UUID id) {
    List<Incident> rows =
        jdbcTemplate.query(
            """
            SELECT id, version, status, title, description, severity, source, created_at, updated_at
            FROM incidents WHERE id = ?
            """,
            ROW_MAPPER,
            id);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.getFirst());
  }

  /**
   * Updates mutable fields for manual incidents: increments {@code version} by 1 on success (CAS on
   * {@code expectedVersion}), sets {@code updated_at} to {@code updatedAt}. Does not reference 1b
   * columns.
   */
  @Override
  public Optional<Incident> updateFields(
      UUID id,
      long expectedVersion,
      String title,
      String description,
      IncidentSeverity severity,
      Instant updatedAt) {
    List<Incident> rows =
        jdbcTemplate.query(
            """
            UPDATE incidents
            SET title = ?, description = ?, severity = ?, version = version + 1, updated_at = ?
            WHERE id = ? AND version = ? AND source = 'MANUAL'
            RETURNING id, version, status, title, description, severity, source, created_at, updated_at
            """,
            ROW_MAPPER,
            title,
            description,
            severity.name(),
            Timestamp.from(updatedAt),
            id,
            expectedVersion);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.getFirst());
  }
}
