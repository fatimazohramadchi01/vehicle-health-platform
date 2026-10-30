package com.renault.analysis.domain.scoring;

import com.renault.analysis.domain.model.AnalysisContext;
import com.renault.analysis.domain.model.Penalty;

import java.util.Optional;

public interface ScoringRule {
    Optional<Penalty> evaluate(AnalysisContext context);
}