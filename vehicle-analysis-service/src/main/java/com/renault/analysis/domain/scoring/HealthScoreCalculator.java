package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.HealthReport;
import com.renault.analysis.domain.model.Penalty;

import java.util.List;
import java.util.Optional;

public class HealthScoreCalculator {

    private final List<ScoringRule> rules;

    public HealthScoreCalculator() {
        this(List.of(
                new MileageRule(),
                new TirePressureRule(),
                new BatteryRule(),
                new EngineTemperatureRule(),
                new FuelRule(),
                new ErrorCodeRule(),
                new RecallCampaignRule()));
    }

    public HealthScoreCalculator(List<ScoringRule> rules) {
        this.rules = rules;
    }

    public HealthReport calculate(AnalysisContext context) {
        List<Penalty> penalties = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();

        int totalPenaltyPoints = penalties.stream()
                .mapToInt(Penalty::points)
                .sum();

        // Les points sont négatifs, donc on AJOUTE : 100 + (-57) = 43
        int score = Math.max(0, Math.min(100, 100 + totalPenaltyPoints));

        return new HealthReport(
                context.snapshot().vin(),
                context.snapshot().measuredAt(),
                score,
                penalties);
    }
}