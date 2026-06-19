package com.teco.bolao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "matches")
public class Match extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TournamentPhase phase;

    @Column(name = "match_number", nullable = false)
    private Integer matchNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;

    @Column(length = 120)
    private String venue;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "official_result_at")
    private OffsetDateTime officialResultAt;

    protected Match() {
    }

    public Match(
            TournamentPhase phase,
            Integer matchNumber,
            Team homeTeam,
            Team awayTeam,
            OffsetDateTime startsAt,
            MatchStatus status,
            String venue
    ) {
        this.phase = phase;
        this.matchNumber = matchNumber;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startsAt = startsAt;
        this.status = status;
        this.venue = venue;
    }

    public TournamentPhase getPhase() {
        return phase;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public String getVenue() {
        return venue;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public OffsetDateTime getOfficialResultAt() {
        return officialResultAt;
    }

    public void updateOfficialResult(Integer homeScore, Integer awayScore, OffsetDateTime officialResultAt) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.officialResultAt = officialResultAt;
        this.status = MatchStatus.FINISHED;
    }
}
