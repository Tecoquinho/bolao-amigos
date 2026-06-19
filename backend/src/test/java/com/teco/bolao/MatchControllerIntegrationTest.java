package com.teco.bolao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
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
class MatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldFilterMatchesByStatus() throws Exception {
        Team brazil = teamRepository.save(new Team("Brasil", "BRA", null));
        Team argentina = teamRepository.save(new Team("Argentina", "ARG", null));
        Team france = teamRepository.save(new Team("Franca", "FRA", null));

        matchRepository.save(new Match(
                TournamentPhase.GROUP_STAGE,
                1,
                brazil,
                argentina,
                OffsetDateTime.parse("2026-06-11T21:00:00Z"),
                MatchStatus.SCHEDULED,
                "Mexico City"
        ));
        Match liveMatch = new Match(
                TournamentPhase.GROUP_STAGE,
                2,
                france,
                brazil,
                OffsetDateTime.parse("2026-06-12T21:00:00Z"),
                MatchStatus.LIVE,
                "Toronto"
        );
        matchRepository.save(liveMatch);

        mockMvc.perform(get("/api/matches").param("status", "LIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matchNumber", is(2)))
                .andExpect(jsonPath("$[0].status", is("LIVE")));
    }

    @Test
    void shouldUpdateOfficialResultAndRecalculatePredictions() throws Exception {
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
        predictionRepository.save(new Prediction(participant, match, 2, 1));

        mockMvc.perform(patch("/api/matches/{matchId}/result", match.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("homeScore", 2, "awayScore", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FINISHED")))
                .andExpect(jsonPath("$.homeScore", is(2)))
                .andExpect(jsonPath("$.awayScore", is(1)));

        Prediction updatedPrediction = predictionRepository.findByParticipantIdAndMatchId(participant.getId(), match.getId())
                .orElseThrow();
        Match updatedMatch = matchRepository.findById(match.getId()).orElseThrow();

        assertNotNull(updatedMatch.getOfficialResultAt());
        assertEquals(MatchStatus.FINISHED, updatedMatch.getStatus());
        assertEquals(5, updatedPrediction.getPointsAwarded());
    }

    @Test
    void shouldReturnBadRequestForNegativeResult() throws Exception {
        mockMvc.perform(patch("/api/matches/{matchId}/result", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("homeScore", -1, "awayScore", 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.homeScore", is("must be greater than or equal to 0")));
    }
}
