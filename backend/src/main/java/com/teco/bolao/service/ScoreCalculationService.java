package com.teco.bolao.service;

import org.springframework.stereotype.Service;

@Service
public class ScoreCalculationService {

    public int calculatePoints(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        validateScore(actualHomeScore);
        validateScore(actualAwayScore);
        validateScore(predictedHomeScore);
        validateScore(predictedAwayScore);

        if (isExactScore(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore)) {
            return 5;
        }

        if (isDraw(actualHomeScore, actualAwayScore)) {
            return isDraw(predictedHomeScore, predictedAwayScore) ? 3 : 0;
        }

        if (hasCorrectGoalsForActualWinner(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore)) {
            return 2;
        }

        if (hasCorrectGoalsForActualLoser(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore)) {
            return 2;
        }

        if (isCorrectOutcome(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore)) {
            return 1;
        }

        return 0;
    }

    public boolean isExactScore(int actualHomeScore, int actualAwayScore, int predictedHomeScore, int predictedAwayScore) {
        return actualHomeScore == predictedHomeScore && actualAwayScore == predictedAwayScore;
    }

    public boolean isCorrectOutcome(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        return Integer.signum(actualHomeScore - actualAwayScore) == Integer.signum(predictedHomeScore - predictedAwayScore);
    }

    private boolean isDraw(int homeScore, int awayScore) {
        return homeScore == awayScore;
    }

    private boolean hasCorrectGoalsForActualWinner(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        return actualWinnerGoals(actualHomeScore, actualAwayScore)
                == predictedGoalsForActualWinner(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore);
    }

    private boolean hasCorrectGoalsForActualLoser(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        return actualLoserGoals(actualHomeScore, actualAwayScore)
                == predictedGoalsForActualLoser(actualHomeScore, actualAwayScore, predictedHomeScore, predictedAwayScore);
    }

    private int actualWinnerGoals(int actualHomeScore, int actualAwayScore) {
        return Math.max(actualHomeScore, actualAwayScore);
    }

    private int actualLoserGoals(int actualHomeScore, int actualAwayScore) {
        return Math.min(actualHomeScore, actualAwayScore);
    }

    private int predictedGoalsForActualWinner(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        return actualHomeScore > actualAwayScore ? predictedHomeScore : predictedAwayScore;
    }

    private int predictedGoalsForActualLoser(
            int actualHomeScore,
            int actualAwayScore,
            int predictedHomeScore,
            int predictedAwayScore
    ) {
        return actualHomeScore > actualAwayScore ? predictedAwayScore : predictedHomeScore;
    }

    private void validateScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
        }
    }
}
