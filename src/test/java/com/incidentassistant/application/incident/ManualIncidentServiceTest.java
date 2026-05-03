package com.incidentassistant.application.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.incidentassistant.domain.incident.CreateManualIncidentCommand;
import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentConflictException;
import com.incidentassistant.domain.incident.IncidentFieldPatch;
import com.incidentassistant.domain.incident.IncidentSeverity;
import com.incidentassistant.domain.incident.IncidentSource;
import com.incidentassistant.domain.incident.IncidentStaleVersionException;
import com.incidentassistant.domain.incident.IncidentStatus;
import com.incidentassistant.domain.incident.IncidentValidationException;
import com.incidentassistant.domain.incident.ManualIncidentRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManualIncidentServiceTest {

  private static final Instant T0 = Instant.parse("2026-05-01T12:00:00Z");

  @Mock private ManualIncidentRepository repository;

  private ManualIncidentService service;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
    service = new ManualIncidentService(repository, clock);
  }

  @Test
  void create_persistsDraftManualWithInitialVersion() {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    Incident row =
        new Incident(
            id,
            ManualIncidentService.INITIAL_VERSION,
            IncidentStatus.DRAFT,
            "t",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    when(repository.insert(any())).thenAnswer(inv -> inv.getArgument(0));

    Incident created =
        service.create(new CreateManualIncidentCommand("  t  ", null, IncidentSeverity.SEV1));

    assertThat(created.id()).isNotNull();
    assertThat(created.version()).isEqualTo(ManualIncidentService.INITIAL_VERSION);
    assertThat(created.status()).isEqualTo(IncidentStatus.DRAFT);
    assertThat(created.source()).isEqualTo(IncidentSource.MANUAL);
    assertThat(created.title()).isEqualTo("t");
    assertThat(created.createdAt()).isEqualTo(T0);
    assertThat(created.updatedAt()).isEqualTo(T0);

    ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
    verify(repository).insert(captor.capture());
    assertThat(captor.getValue().source()).isEqualTo(IncidentSource.MANUAL);
  }

  @Test
  void create_rejectsInvalidTitle() {
    assertThatThrownBy(() -> service.create(new CreateManualIncidentCommand("", null, IncidentSeverity.SEV1)))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void updateFields_rejectsWhenClosedOrCancelled() {
    UUID id = UUID.randomUUID();
    Incident closed =
        new Incident(
            id,
            2L,
            IncidentStatus.CLOSED,
            "t",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    when(repository.findById(id)).thenReturn(Optional.of(closed));

    assertThatThrownBy(
            () ->
                service.updateFields(
                    id,
                    2L,
                    new IncidentFieldPatch(Optional.of("x"), Optional.empty(), Optional.empty())))
        .isInstanceOf(IncidentConflictException.class)
        .hasMessageContaining("CLOSED");

    verify(repository).findById(id);
    verify(repository, org.mockito.Mockito.never()).updateFields(any(), anyLong(), any(), any(), any(), any());
  }

  @Test
  void updateFields_rejectsCancelledSeverityChange() {
    UUID id = UUID.randomUUID();
    Incident cancelled =
        new Incident(
            id,
            1L,
            IncidentStatus.CANCELLED,
            "t",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    when(repository.findById(id)).thenReturn(Optional.of(cancelled));

    assertThatThrownBy(
            () ->
                service.updateFields(
                    id,
                    1L,
                    new IncidentFieldPatch(
                        Optional.empty(), Optional.empty(), Optional.of(IncidentSeverity.SEV2))))
        .isInstanceOf(IncidentConflictException.class)
        .hasMessageContaining("CANCELLED");
  }

  @Test
  void updateFields_appliesWhenDraft() {
    UUID id = UUID.randomUUID();
    Incident draft =
        new Incident(
            id,
            1L,
            IncidentStatus.DRAFT,
            "old",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    Incident updated =
        new Incident(
            id,
            2L,
            IncidentStatus.DRAFT,
            "new",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    when(repository.findById(id)).thenReturn(Optional.of(draft));
    when(repository.updateFields(eq(id), eq(1L), eq("new"), eq(null), eq(IncidentSeverity.SEV1), eq(T0)))
        .thenReturn(Optional.of(updated));

    Incident out =
        service.updateFields(
            id, 1L, new IncidentFieldPatch(Optional.of("new"), Optional.empty(), Optional.empty()));

    assertThat(out.version()).isEqualTo(2L);
    assertThat(out.title()).isEqualTo("new");
  }

  @Test
  void updateFields_throwsWhenPatchEmpty() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(() -> service.updateFields(id, 1L, IncidentFieldPatch.empty()))
        .isInstanceOf(IncidentValidationException.class);
  }

  @Test
  void updateFields_throwsStaleWhenRepositoryMissesVersion() {
    UUID id = UUID.randomUUID();
    Incident draft =
        new Incident(
            id,
            1L,
            IncidentStatus.DRAFT,
            "old",
            null,
            IncidentSeverity.SEV1,
            IncidentSource.MANUAL,
            T0,
            T0);
    when(repository.findById(id)).thenReturn(Optional.of(draft));
    when(repository.updateFields(eq(id), eq(1L), any(), any(), any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.updateFields(
                    id,
                    1L,
                    new IncidentFieldPatch(Optional.of("new"), Optional.empty(), Optional.empty())))
        .isInstanceOf(IncidentStaleVersionException.class);
  }
}
