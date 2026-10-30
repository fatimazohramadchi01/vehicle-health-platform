package com.renault.analysis.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PenaltyEmbeddable {

    @Column(name = "rule_name", nullable = false)
    private String rule;

    @Column(name = "points", nullable = false)
    private int points;

    protected PenaltyEmbeddable() {
    }

    public PenaltyEmbeddable(String rule, int points) {
        this.rule = rule;
        this.points = points;
    }

    public String getRule() { return rule; }
    public int getPoints() { return points; }
}