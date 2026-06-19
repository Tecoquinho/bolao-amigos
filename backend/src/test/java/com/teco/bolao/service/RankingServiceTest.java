package com.teco.bolao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.teco.bolao.dto.RankingEntryResponseDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.entity.Team;
import com.teco.bolao.entity.TournamentPhase;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private ParticipantRepository participantRepository;

    private RankingService rankingService;
    private ScoreCalculationService scoreCalculationService;

    @BeforeEach
    void setUp() {
        scoreCalculationService = new ScoreCalculationService();
        rankingService = new RankingService(predictionRepository, participantRepository, scoreCalculationService);
    }

    @Test
    void shouldOrderRankingByPointsExactScoresResultHitsAndName() {
        Participant ana = participant(1L, "Ana");
        Participant carlos = participant(2L, "Carlos");
        Participant beatriz = participant(3L, "Beatriz");
        Participant bruno = participant(4L, "Bruno");
        Participant davi = participant(5L, "Davi");

        Match match1 = finishedMatch(1L, 1, 1, 1);
        Match match2 = finishedMatch(2L, 1, 0, 2);
        Match match3 = finishedMatch(3L, 2, 1, 3);
        Match match4 = finishedMatch(4L, 3, 1, 4);
        Match match5 = finishedMatch(5L, 0, 0, 5);

        List<Prediction> predictions = List.of(
                prediction(ana, match1, 1, 1),
                prediction(ana, match2, 2, 1),
                prediction(ana, match3, 3, 2),
                prediction(ana, match4, 4, 0),
                prediction(ana, match5, 1, 0),

                prediction(carlos, match1, 1, 1),
                prediction(carlos, match2, 0, 1),
                prediction(carlos, match3, 0, 2),
                prediction(carlos, match4, 1, 3),
                prediction(carlos, match5, 2, 2),

                prediction(beatriz, match1, 2, 1),
                prediction(beatriz, match2, 2, 1),
                prediction(beatriz, match3, 1, 0),
                prediction(beatriz, match4, 0, 0),
                prediction(beatriz, match5, 1, 1),

                prediction(bruno, match1, 2, 1),
                prediction(bruno, match2, 3, 2),
                prediction(bruno, match3, 1, 0),
                prediction(bruno, match4, 0, 0),
                prediction(bruno, match5, 1, 1)
        );

        when(participantRepository.findAllByOrderByNameAsc()).thenReturn(List.of(ana, beatriz, bruno, carlos, davi));
        when(predictionRepository.findAll()).thenReturn(predictions);

        List<RankingEntryResponseDto> ranking = rankingService.getRanking();

        assertEquals(List.of("Ana", "Carlos", "Beatriz", "Bruno", "Davi"),
                ranking.stream().map(RankingEntryResponseDto::participantName).toList());
        assertEquals(List.of(8, 8, 5, 5, 0),
                ranking.stream().map(RankingEntryResponseDto::totalPoints).toList());
        assertEquals(List.of(1, 1, 0, 0, 0),
                ranking.stream().map(RankingEntryResponseDto::exactScoreHits).toList());
        assertEquals(List.of(4, 2, 3, 3, 0),
                ranking.stream().map(RankingEntryResponseDto::resultHits).toList());
        assertEquals(List.of(1, 2, 3, 4, 5),
                ranking.stream().map(RankingEntryResponseDto::position).toList());
    }

    @Test
    void shouldIgnoreScheduledPredictionsEvenWhenPointsAwardedIsGreaterThanZero() {
        Participant ana = participant(1L, "Ana");
        Match scheduledMatch = match(1L, MatchStatus.SCHEDULED, 2, 1, 1);
        Prediction scheduledPrediction = prediction(ana, scheduledMatch, 2, 1);
        scheduledPrediction.updatePointsAwarded(5);

        when(participantRepository.findAllByOrderByNameAsc()).thenReturn(List.of(ana));
        when(predictionRepository.findAll()).thenReturn(List.of(scheduledPrediction));

        List<RankingEntryResponseDto> ranking = rankingService.getRanking();

        assertEquals(List.of("Ana"), ranking.stream().map(RankingEntryResponseDto::participantName).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::totalPoints).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::exactScoreHits).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::resultHits).toList());
    }

    @Test
    void shouldIgnoreLivePredictionsEvenWhenPointsAwardedIsGreaterThanZero() {
        Participant ana = participant(1L, "Ana");
        Match liveMatch = match(1L, MatchStatus.LIVE, 1, 1, 1);
        Prediction livePrediction = prediction(ana, liveMatch, 1, 1);
        livePrediction.updatePointsAwarded(5);

        when(participantRepository.findAllByOrderByNameAsc()).thenReturn(List.of(ana));
        when(predictionRepository.findAll()).thenReturn(List.of(livePrediction));

        List<RankingEntryResponseDto> ranking = rankingService.getRanking();

        assertEquals(List.of("Ana"), ranking.stream().map(RankingEntryResponseDto::participantName).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::totalPoints).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::exactScoreHits).toList());
        assertEquals(List.of(0), ranking.stream().map(RankingEntryResponseDto::resultHits).toList());
    }

    private Participant participant(Long id, String name) {
        Participant participant = new Participant(name);
        ReflectionTestUtils.setField(participant, "id", id);
        return participant;
    }

    private Match finishedMatch(Long id, int homeScore, int awayScore, int matchNumber) {
        Match match = match(id, MatchStatus.SCHEDULED, homeScore, awayScore, matchNumber);
        match.updateOfficialResult(homeScore, awayScore, OffsetDateTime.parse("2026-06-12T21:00:00Z"));
        return match;
    }

    private Match match(Long id, MatchStatus status, int homeScore, int awayScore, int matchNumber) {
        Match match = new Match(
                TournamentPhase.GROUP_STAGE,
                matchNumber,
                new Team("Home " + matchNumber, "H" + matchNumber, null),
                new Team("Away " + matchNumber, "A" + matchNumber, null),
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                status,
                "Venue"
        );
        ReflectionTestUtils.setField(match, "id", id);
        ReflectionTestUtils.setField(match, "homeScore", homeScore);
        ReflectionTestUtils.setField(match, "awayScore", awayScore);
        return match;
    }

    private Prediction prediction(Participant participant, Match match, int predictedHomeScore, int predictedAwayScore) {
        Prediction prediction = new Prediction(participant, match, predictedHomeScore, predictedAwayScore);
        prediction.updatePointsAwarded(scoreCalculationService.calculatePoints(
                match.getHomeScore(),
                match.getAwayScore(),
                predictedHomeScore,
                predictedAwayScore
        ));
        return prediction;
    }
}
