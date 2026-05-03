package com.incidentassistant.incident.persistence;

import com.incidentassistant.incident.domain.IncidentSeverity;
import com.incidentassistant.incident.domain.IncidentSource;
import com.incidentassistant.incident.domain.IncidentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Maps the {@code incidents} table. Phase 1a does not map 1b-only columns; they stay null in SQL.
 */
@Entity
@Table(name = "incidents")
public class IncidentEntity {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Version
  @Column(nullable = false)
  private long version;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private IncidentStatus status;

  @Column(nullable = false, length = 200)
  private String title;

  @Column private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private IncidentSeverity severity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private IncidentSource source;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected IncidentEntity() {}

  public IncidentEntity(
      UUID id,
      IncidentStatus status,
      String title,
      String description,
      IncidentSeverity severity,
      IncidentSource source) {
    this.id = id;
    this.status = status;
    this.title = title;
    this.description = description;
    this.severity = severity;
    this.source = source;
  }

  public UUID getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  public IncidentStatus getStatus() {
    return status;
  }

  public void setStatus(IncidentStatus status) {
    this.status = status;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public IncidentSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(IncidentSeverity severity) {
    this.severity = severity;
  }

  public IncidentSource getSource() {
    return source;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
