package com.teco.bolao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teco.bolao.service.ScoreCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreCalculationServiceTest {

    private ScoreCalculationService scoreCalculationService;

    @BeforeEach
    void setUp() {
        scoreCalculationService = new ScoreCalculationService();
    }

    @Test
    void shouldReturnFivePointsForExactScoreWithWinner() {
        assertEquals(5, scoreCalculationService.calculatePoints(2, 1, 2, 1));
    }

    @Test
    void shouldReturnFivePointsForExactScoreWithDraw() {
        assertEquals(5, scoreCalculationService.calculatePoints(0, 0, 0, 0));
    }

    @Test
    void shouldReturnTwoPointsWhenWinnerGoalsAreCorrect() {
        assertEquals(2, scoreCalculationService.calculatePoints(2, 1, 2, 0));
    }

    @Test
    void shouldReturnTwoPointsWhenLoserGoalsAreCorrect() {
        assertEquals(2, scoreCalculationService.calculatePoints(2, 1, 3, 1));
    }

    @Test
    void shouldReturnOnePointWhenOnlyWinnerIsCorrect() {
        assertEquals(1, scoreCalculationService.calculatePoints(2, 1, 4, 2));
    }

    @Test
    void shouldReturnZeroWhenWinnerMatchIsCompletelyWrong() {
        assertEquals(0, scoreCalculationService.calculatePoints(2, 1, 0, 0));
    }

    @Test
    void shouldReturnThreePointsForDrawWithoutExactScore() {
        assertEquals(3, scoreCalculationService.calculatePoints(1, 1, 2, 2));
    }

    @Test
    void shouldReturnZeroWhenActualMatchIsDrawAndPredictionIsNotDraw() {
        assertEquals(0, scoreCalculationService.calculatePoints(1, 1, 2, 1));
    }

    @Test
    void shouldApplyHighestPossibleScoreOnlyWhenWinnerGoalsAlsoImplyCorrectWinner() {
        assertEquals(2, scoreCalculationService.calculatePoints(3, 1, 3, 0));
    }

    @Test
    void shouldReturnTwoPointsWhenOnlyActualLoserGoalsMatch() {
        assertEquals(2, scoreCalculationService.calculatePoints(2, 1, 0, 1));
    }

    @Test
    void shouldReturnTwoPointsWhenActualAwayTeamWinsAndWinnerGoalsAreCorrect() {
        assertEquals(2, scoreCalculationService.calculatePoints(1, 3, 0, 3));
    }

    @Test
    void shouldReturnZeroWhenPredictionIsDrawAndActualMatchHasWinner() {
        assertEquals(0, scoreCalculationService.calculatePoints(2, 1, 1, 1));
    }

    @Test
    void shouldReturnZeroWhenPredictionIsDrawAndMatchesActualWinnerGoals() {
        assertEquals(0, scoreCalculationService.calculatePoints(3, 1, 3, 3));
    }

    @Test
    void shouldReturnZeroWhenPredictionIsDrawAndMatchesActualLoserGoals() {
        assertEquals(0, scoreCalculationService.calculatePoints(2, 1, 1, 1));
    }

    @Test
    void shouldThrowExceptionForNegativeScores() {
        assertThrows(IllegalArgumentException.class, () -> scoreCalculationService.calculatePoints(2, 1, -1, 0));
    }
}
