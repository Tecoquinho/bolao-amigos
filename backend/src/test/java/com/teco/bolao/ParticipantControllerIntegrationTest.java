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
class ParticipantControllerIntegrationTest {

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
    void shouldListParticipantsOrderedByName() throws Exception {
        participantRepository.save(new Participant("Carlos"));
        participantRepository.save(new Participant("Ana"));

        mockMvc.perform(get("/api/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Ana")))
                .andExpect(jsonPath("$[1].name", is("Carlos")));
    }

    @Test
    void shouldReturnParticipantPredictions() throws Exception {
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
        match.updateOfficialResult(2, 1, OffsetDateTime.parse("2026-06-12T12:00:00Z"));
        matchRepository.save(match);

        Prediction prediction = predictionRepository.save(new Prediction(participant, match, 2, 1));
        prediction.updatePointsAwarded(5);
        predictionRepository.save(prediction);

        mockMvc.perform(get("/api/participants/{participantId}/predictions", participant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].homeTeamName", is("Brasil")))
                .andExpect(jsonPath("$[0].awayTeamName", is("Argentina")))
                .andExpect(jsonPath("$[0].officialHomeScore", is(2)))
                .andExpect(jsonPath("$[0].officialAwayScore", is(1)))
                .andExpect(jsonPath("$[0].pointsAwarded", is(5)));
    }

    @Test
    void shouldReturnNotFoundForMissingParticipant() throws Exception {
        mockMvc.perform(get("/api/participants/{participantId}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Participant not found: 999")));
    }
}
