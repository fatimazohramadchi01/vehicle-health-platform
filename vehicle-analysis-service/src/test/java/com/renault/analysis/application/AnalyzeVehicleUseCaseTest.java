package com.renault.analysis.application;

import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.RecallCampaign;
import com.renault.analysis.domain.model.StoredHealthReport;
import com.renault.analysis.domain.ports.HealthReportStore;
import com.renault.analysis.domain.ports.RecallProvider;
import com.renault.analysis.domain.scoring.AlertGenerator;
import com.renault.analysis.domain.scoring.HealthScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeVehicleUseCaseTest {

    private static final UUID TELEMETRY_ID = UUID.fromString("d4f1c2e8-1234-5678-9abc-def012345678");
    private static final String VIN = "VF1RFA00567891234";
    private static final Instant MEASURED_AT = Instant.parse("2026-10-29T10:15:00Z");

    @Mock
    private RecallProvider recallProvider;

    @Mock
    private HealthReportStore reportStore;

    private AnalyzeVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AnalyzeVehicleUseCase(recallProvider, reportStore,
                new HealthScoreCalculator(), new AlertGenerator());
    }

    @Test
    void planExampleIsAnalyzedScoredAndStored() {
        when(recallProvider.activeCampaignsFor(VIN)).thenReturn(List.of(
                new RecallCampaign("RC-2026-014", "Contrôle faisceau électrique")));
        when(reportStore.findByTelemetryId(TELEMETRY_ID)).thenReturn(Optional.empty());

        useCase.analyze(planExampleCommand());

        ArgumentCaptor<StoredHealthReport> captor = ArgumentCaptor.forClass(StoredHealthReport.class);
        verify(reportStore).save(captor.capture());
        StoredHealthReport stored = captor.getValue();

        assertThat(stored.telemetryId()).isEqualTo(TELEMETRY_ID);
        assertThat(stored.report().vin()).isEqualTo(VIN);
        assertThat(stored.report().measuredAt()).isEqualTo(MEASURED_AT);
        assertThat(stored.report().score()).isEqualTo(43);
        assertThat(stored.report().penalties()).hasSize(7);
        assertThat(stored.alerts()).hasSize(8);
    }

    @Test
    void replayingSameTelemetryIsIdempotent() {
        StoredHealthReport alreadyStored =
                new StoredHealthReport(TELEMETRY_ID,
                        new HealthReport(VIN, MEASURED_AT, 43, List.of()), List.of());

        when(recallProvider.activeCampaignsFor(VIN)).thenReturn(List.of(
                new RecallCampaign("RC-2026-014", "Contrôle faisceau électrique")));
        when(reportStore.findByTelemetryId(TELEMETRY_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(alreadyStored));

        StoredHealthReport first = useCase.analyze(planExampleCommand());
        StoredHealthReport second = useCase.analyze(planExampleCommand());

        assertThat(first.report().score()).isEqualTo(43);
        assertThat(second).isSameAs(alreadyStored);
        verify(recallProvider, times(1)).activeCampaignsFor(VIN);
        verify(reportStore, times(1)).save(any());
    }

    @Test
    void providerFailureSavesNothing() {
        RuntimeException failure = new RuntimeException("provider unavailable");
        when(reportStore.findByTelemetryId(TELEMETRY_ID)).thenReturn(Optional.empty());
        when(recallProvider.activeCampaignsFor(VIN)).thenThrow(failure);

        assertThatThrownBy(() -> useCase.analyze(planExampleCommand()))
                .isSameAs(failure);

        verify(reportStore, never()).save(any());
    }

    private AnalyzeVehicleCommand planExampleCommand() {
        return new AnalyzeVehicleCommand(
                TELEMETRY_ID,
                VIN,
                MEASURED_AT,
                125_000,
                List.of(2.1, 2.1, 2.4, 2.4),
                11.8,
                118,
                8,
                List.of("P0301", "P0301"));
    }
}