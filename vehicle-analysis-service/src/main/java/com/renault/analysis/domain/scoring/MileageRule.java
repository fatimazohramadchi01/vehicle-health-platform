package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class MileageRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        long mileageKm = context.snapshot().mileageKm();
        if (mileageKm <= 100_000) {
            return Optional.empty();
        }
        long completeBrackets = (mileageKm - 100_000) / 25_000; // division entière : tranches complètes
        int points = (int) Math.max(completeBrackets * -5, -20);
        return points == 0
                ? Optional.empty()
                : Optional.of(new Penalty(ScoringRuleType.MILEAGE, points));
    }
}