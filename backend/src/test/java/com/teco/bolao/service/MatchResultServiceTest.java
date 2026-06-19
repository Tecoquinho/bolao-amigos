package com.teco.bolao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.teco.bolao.dto.MatchResponseDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.entity.Team;
import com.teco.bolao.entity.TournamentPhase;
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
class MatchResultServiceTest {

    @Mock
    private MatchService matchService;

    @Mock
    private PredictionRepository predictionRepository;

    private MatchResultService matchResultService;

    @BeforeEach
    void setUp() {
        PredictionService predictionService = new PredictionService(predictionRepository, new ScoreCalculationService());
        matchResultService = new MatchResultService(matchService, predictionService);
    }

    @Test
    void shouldRecalculatePointsWhenOfficialResultChangesFromWinToDraw() {
        Match match = finishedMatch(1L, 2, 1);
        Prediction prediction = prediction(match, 2, 1);
        prediction.updatePointsAwarded(5);

        mockUpdate(match, prediction);

        MatchResponseDto result = matchResultService.updateOfficialResult(1L, 1, 1);

        assertSame(match.getId(), result.id());
        assertEquals(0, prediction.getPointsAwarded());
        assertEquals(1, match.getHomeScore());
        assertEquals(1, match.getAwayScore());
    }

    @Test
    void shouldAwardFivePointsForExactDrawAfterOfficialResultUpdate() {
        Match match = scheduledMatch(1L);
        Prediction prediction = prediction(match, 1, 1);

        mockUpdate(match, prediction);

        MatchResponseDto result = matchResultService.updateOfficialResult(1L, 1, 1);

        assertSame(match.getId(), result.id());
        assertEquals(5, prediction.getPointsAwarded());
        assertEquals(MatchStatus.FINISHED, match.getStatus());
        assertNotNull(match.getOfficialResultAt());
    }

    @Test
    void shouldAwardThreePointsForNonExactDrawAfterOfficialResultUpdate() {
        Match match = scheduledMatch(1L);
        Prediction prediction = prediction(match, 2, 2);

        mockUpdate(match, prediction);

        matchResultService.updateOfficialResult(1L, 1, 1);

        assertEquals(3, prediction.getPointsAwarded());
    }

    @Test
    void shouldTurnCorrectWinnerIntoErrorAfterOfficialResultCorrection() {
        Match match = finishedMatch(1L, 2, 1);
        Prediction prediction = prediction(match, 4, 3);
        prediction.updatePointsAwarded(1);

        mockUpdate(match, prediction);

        matchResultService.updateOfficialResult(1L, 1, 2);

        assertEquals(0, prediction.getPointsAwarded());
    }

    @Test
    void shouldDecreasePointsAfterOfficialScoreCorrection() {
        Match match = finishedMatch(1L, 2, 1);
        Prediction prediction = prediction(match, 2, 1);
        prediction.updatePointsAwarded(5);

        mockUpdate(match, prediction);

        matchResultService.updateOfficialResult(1L, 2, 0);

        assertEquals(2, prediction.getPointsAwarded());
    }

    private void mockUpdate(Match match, Prediction prediction) {
        when(matchService.getMatchEntity(match.getId())).thenReturn(match);
        when(matchService.toResponse(match)).thenAnswer(invocation -> toResponse(match));
        when(predictionRepository.findByMatchId(match.getId())).thenReturn(List.of(prediction));
    }

    private Match scheduledMatch(Long id) {
        Match match = new Match(
                TournamentPhase.GROUP_STAGE,
                1,
                new Team("Brasil", "BRA", null),
                new Team("Argentina", "ARG", null),
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Mexico City"
        );
        ReflectionTestUtils.setField(match, "id", id);
        return match;
    }

    private Match finishedMatch(Long id, int homeScore, int awayScore) {
        Match match = scheduledMatch(id);
        match.updateOfficialResult(homeScore, awayScore, OffsetDateTime.parse("2026-06-12T12:00:00Z"));
        return match;
    }

    private Prediction prediction(Match match, int predictedHomeScore, int predictedAwayScore) {
        Participant participant = new Participant("Matheus");
        return new Prediction(participant, match, predictedHomeScore, predictedAwayScore);
    }

    private MatchResponseDto toResponse(Match match) {
        return new MatchResponseDto(
                match.getId(),
                match.getPhase(),
                match.getMatchNumber(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getFifaCode(),
                match.getHomeTeam().getFlagUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getFifaCode(),
                match.getAwayTeam().getFlagUrl(),
                match.getStartsAt(),
                match.getStatus(),
                match.getVenue(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getOfficialResultAt()
        );
    }
}
