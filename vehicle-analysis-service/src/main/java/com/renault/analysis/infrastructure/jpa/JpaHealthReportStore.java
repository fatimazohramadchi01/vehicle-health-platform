package com.renault.analysis.infrastructure.jpa;

import com.renault.analysis.domain.model.Alert;
import com.renault.analysis.domain.model.AlertType;
import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;
import com.renault.analysis.domain.model.Severity;
import com.renault.analysis.domain.model.StoredHealthReport;
import com.renault.analysis.domain.ports.HealthReportStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class JpaHealthReportStore implements HealthReportStore {

    private final HealthReportJpaRepository repository;

    public JpaHealthReportStore(HealthReportJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(StoredHealthReport report) {
        HealthReportJpaEntity entity = new HealthReportJpaEntity();
        entity.setTelemetryId(report.telemetryId());
        HealthReport domainReport = report.report();
        entity.setVin(domainReport.vin());
        entity.setMeasuredAt(domainReport.measuredAt());
        entity.setScore(domainReport.score());

        domainReport.penalties().forEach(penalty ->
                entity.getPenalties().add(new PenaltyEmbeddable(penalty.rule().name(), penalty.points())));
        report.alerts().forEach(alert -> {
            AlertJpaEntity alertEntity = new AlertJpaEntity();
            alertEntity.setReport(entity);
            alertEntity.setType(alert.type().name());
            alertEntity.setSeverity(alert.severity().name());
            alertEntity.setMessage(alert.message());
            entity.getAlerts().add(alertEntity);
        });

        repository.save(entity);
    }

    @Override
    public Optional<StoredHealthReport> findByTelemetryId(UUID telemetryId) {
        return repository.findByTelemetryId(telemetryId).map(this::toDomain);
    }

    @Override
    public Optional<StoredHealthReport> findLatestByVin(String vin) {
        return repository.findTopByVinOrderByMeasuredAtDescTelemetryIdDesc(vin).map(this::toDomain);
    }

    private StoredHealthReport toDomain(HealthReportJpaEntity entity) {
        List<Penalty> penalties = entity.getPenalties().stream()
                .map(p -> new Penalty(ScoringRuleType.valueOf(p.getRule()), p.getPoints()))
                .toList();
        List<Alert> alerts = entity.getAlerts().stream()
                .map(a -> new Alert(AlertType.valueOf(a.getType()), Severity.valueOf(a.getSeverity()), a.getMessage()))
                .toList();
        HealthReport report = new HealthReport(entity.getVin(), entity.getMeasuredAt(), entity.getScore(), penalties);
        return new StoredHealthReport(entity.getTelemetryId(), report, alerts);
    }
}