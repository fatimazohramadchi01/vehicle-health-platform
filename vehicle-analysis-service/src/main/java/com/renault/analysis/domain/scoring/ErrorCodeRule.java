package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;
import com.renault.analysis.domain.model.ScoringRuleType;

import java.util.Optional;

public class ErrorCodeRule implements ScoringRule {

    @Override
    public Optional<Penalty> evaluate(AnalysisContext context) {
        long uniqueCodes = context.snapshot().errorCodes().stream()
                .distinct()
                .count();
        int points = (int) Math.max(uniqueCodes * -5, -20);
        return points == 0
                ? Optional.empty()
                : Optional.of(new Penalty(ScoringRuleType.OBD_ERROR, points));
    }
}