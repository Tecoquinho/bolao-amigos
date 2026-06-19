package com.teco.bolao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.entity.Team;
import com.teco.bolao.entity.TournamentPhase;
import com.teco.bolao.repository.MatchRepository;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import com.teco.bolao.repository.TeamRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PredictionRepositoryIntegrationTest {

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    void shouldPersistPredictionAndFindItByParticipantAndMatch() {
        Participant participant = participantRepository.save(new Participant("Matheus"));
        Team homeTeam = teamRepository.save(new Team("Brasil", "BRA", null));
        Team awayTeam = teamRepository.save(new Team("Argentina", "ARG", null));
        Match match = matchRepository.save(new Match(
                TournamentPhase.GROUP_STAGE,
                1,
                homeTeam,
                awayTeam,
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Mexico City"
        ));

        Prediction savedPrediction = predictionRepository.save(new Prediction(participant, match, 2, 1));

        assertTrue(predictionRepository.findByParticipantIdAndMatchId(participant.getId(), match.getId()).isPresent());
        assertEquals(0, savedPrediction.getPointsAwarded());
    }

    @Test
    void shouldEnforceUniquePredictionPerParticipantAndMatch() {
        Participant participant = participantRepository.save(new Participant("Matheus"));
        Team homeTeam = teamRepository.save(new Team("Brasil", "BRA", null));
        Team awayTeam = teamRepository.save(new Team("Argentina", "ARG", null));
        Match match = matchRepository.save(new Match(
                TournamentPhase.GROUP_STAGE,
                1,
                homeTeam,
                awayTeam,
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Mexico City"
        ));

        predictionRepository.saveAndFlush(new Prediction(participant, match, 2, 1));

        try {
            predictionRepository.saveAndFlush(new Prediction(participant, match, 1, 0));
        } catch (DataIntegrityViolationException exception) {
            return;
        }

        throw new AssertionError("Expected unique constraint violation for participant and match");
    }
}
