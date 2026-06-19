package com.teco.bolao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "predictions")
public class Prediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "predicted_home_score", nullable = false)
    private Integer predictedHomeScore;

    @Column(name = "predicted_away_score", nullable = false)
    private Integer predictedAwayScore;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    protected Prediction() {
    }

    public Prediction(
            Participant participant,
            Match match,
            Integer predictedHomeScore,
            Integer predictedAwayScore
    ) {
        this.participant = participant;
        this.match = match;
        this.predictedHomeScore = predictedHomeScore;
        this.predictedAwayScore = predictedAwayScore;
        this.pointsAwarded = 0;
    }

    public Participant getParticipant() {
        return participant;
    }

    public Match getMatch() {
        return match;
    }

    public Integer getPredictedHomeScore() {
        return predictedHomeScore;
    }

    public Integer getPredictedAwayScore() {
        return predictedAwayScore;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void updatePointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }
}
