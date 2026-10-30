package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class TirePressureRule implements ScoringRule {

    private static final double MIN_PRESSURE_BAR = 2.2;
    private static final double MAX_PRESSURE_BAR = 2.8;

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        long faultyTires = context.snapshot().tirePressuresBar().stream()
                .filter(p -> p < MIN_PRESSURE_BAR || p > MAX_PRESSURE_BAR)
                .count();
        int points = (int) Math.max(faultyTires * -5, -20);
        return points == 0
                ? Optional.empty()
                : Optional.of(new Penalty(ScoringRuleType.TIRE_PRESSURE, points));
    }
}