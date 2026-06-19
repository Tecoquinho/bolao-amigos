package com.teco.bolao;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RankingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        predictionRepository.deleteAll();
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        participantRepository.deleteAll();
    }

    @Test
    void shouldReturnRankingOrderedByRules() throws Exception {
        Participant ana = participantRepository.save(new Participant("Ana"));
        Participant bruno = participantRepository.save(new Participant("Bruno"));
        Participant carlos = participantRepository.save(new Participant("Carlos"));

        Team brazil = teamRepository.save(new Team("Brasil", "BRA", null));
        Team argentina = teamRepository.save(new Team("Argentina", "ARG", null));
        Team france = teamRepository.save(new Team("Franca", "FRA", null));
        Team germany = teamRepository.save(new Team("Alemanha", "GER", null));

        Match match1 = matchRepository.save(new Match(
                TournamentPhase.GROUP_STAGE,
                1,
                brazil,
                argentina,
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Mexico City"
        ));
        match1.updateOfficialResult(1, 0, OffsetDateTime.parse("2026-06-12T12:00:00Z"));
        matchRepository.save(match1);

        Match match2 = matchRepository.save(new Match(
                TournamentPhase.GROUP_STAGE,
                2,
                france,
                germany,
                OffsetDateTime.parse("2026-06-12T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Toronto"
        ));
        match2.updateOfficialResult(2, 2, OffsetDateTime.parse("2026-06-13T12:00:00Z"));
        matchRepository.save(match2);

        Prediction anaPrediction1 = predictionRepository.save(new Prediction(ana, match1, 1, 0));
        anaPrediction1.updatePointsAwarded(5);
        predictionRepository.save(anaPrediction1);
        Prediction anaPrediction2 = predictionRepository.save(new Prediction(ana, match2, 0, 0));
        anaPrediction2.updatePointsAwarded(3);
        predictionRepository.save(anaPrediction2);

        Prediction brunoPrediction1 = predictionRepository.save(new Prediction(bruno, match1, 2, 1));
        brunoPrediction1.updatePointsAwarded(1);
        predictionRepository.save(brunoPrediction1);
        Prediction brunoPrediction2 = predictionRepository.save(new Prediction(bruno, match2, 1, 1));
        brunoPrediction2.updatePointsAwarded(3);
        predictionRepository.save(brunoPrediction2);

        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].participantName", is("Ana")))
                .andExpect(jsonPath("$[0].totalPoints", is(8)))
                .andExpect(jsonPath("$[0].exactScoreHits", is(1)))
                .andExpect(jsonPath("$[0].resultHits", is(2)))
                .andExpect(jsonPath("$[1].participantName", is("Bruno")))
                .andExpect(jsonPath("$[1].totalPoints", is(4)))
                .andExpect(jsonPath("$[2].participantName", is("Carlos")))
                .andExpect(jsonPath("$[2].totalPoints", is(0)));
    }
}
