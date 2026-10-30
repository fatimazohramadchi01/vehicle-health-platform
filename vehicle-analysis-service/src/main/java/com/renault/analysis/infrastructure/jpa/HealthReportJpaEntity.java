package com.renault.analysis.infrastructure.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "health_report")
public class HealthReportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "telemetry_id", nullable = false, unique = true)
    private UUID telemetryId;

    @Column(name = "vin", nullable = false)
    private String vin;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "score", nullable = false)
    private int score;

    @ElementCollection
    @CollectionTable(name = "report_penalty", joinColumns = @JoinColumn(name = "report_id"))
    private List<PenaltyEmbeddable> penalties = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlertJpaEntity> alerts = new ArrayList<>();

    protected HealthReportJpaEntity() {
    }

    public UUID getId() { return id; }
    public UUID getTelemetryId() { return telemetryId; }
    public void setTelemetryId(UUID telemetryId) { this.telemetryId = telemetryId; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public Instant getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(Instant measuredAt) { this.measuredAt = measuredAt; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<PenaltyEmbeddable> getPenalties() { return penalties; }
    public List<AlertJpaEntity> getAlerts() { return alerts; }
}