package com.teco.bolao;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.repository.MatchRepository;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import com.teco.bolao.repository.TeamRepository;
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
class AdminImportControllerIntegrationTest {

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
    void shouldImportSeedAndCalculatePredictionPointsForFinishedMatch() throws Exception {
        String payload = """
                {
                  "participants": [{"name": "Ana"}],
                  "teams": [
                    {"name": "Brasil", "fifaCode": "BRA"},
                    {"name": "Argentina", "fifaCode": "ARG"}
                  ],
                  "matches": [
                    {
                      "phase": "GROUP_STAGE",
                      "matchNumber": 1,
                      "homeTeamCode": "BRA",
                      "awayTeamCode": "ARG",
                      "startsAt": "2026-06-11T21:00:00Z",
                      "status": "FINISHED",
                      "venue": "Mexico City",
                      "homeScore": 2,
                      "awayScore": 1
                    }
                  ],
                  "predictions": [
                    {
                      "participantName": "Ana",
                      "matchNumber": 1,
                      "predictedHomeScore": 2,
                      "predictedAwayScore": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/admin/imports/seed")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCreated", is(1)))
                .andExpect(jsonPath("$.teamsCreated", is(2)))
                .andExpect(jsonPath("$.matchesCreated", is(1)))
                .andExpect(jsonPath("$.predictionsCreated", is(1)))
                .andExpect(jsonPath("$.predictionsSkipped", is(0)));

        Participant participant = participantRepository.findByName("Ana").orElseThrow();
        Match match = matchRepository.findByMatchNumber(1).orElseThrow();
        Prediction prediction = predictionRepository.findByParticipantIdAndMatchId(participant.getId(), match.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(1, participantRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(2, teamRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, matchRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, predictionRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(5, prediction.getPointsAwarded());
    }

    @Test
    void shouldSkipDuplicatesWhenImportingSamePayloadAgain() throws Exception {
        String payload = """
                {
                  "participants": [{"name": "Ana"}],
                  "teams": [
                    {"name": "Brasil", "fifaCode": "BRA"},
                    {"name": "Argentina", "fifaCode": "ARG"}
                  ],
                  "matches": [
                    {
                      "phase": "GROUP_STAGE",
                      "matchNumber": 1,
                      "homeTeamCode": "BRA",
                      "awayTeamCode": "ARG",
                      "startsAt": "2026-06-11T21:00:00Z",
                      "status": "SCHEDULED",
                      "venue": "Mexico City"
                    }
                  ],
                  "predictions": [
                    {
                      "participantName": "Ana",
                      "matchNumber": 1,
                      "predictedHomeScore": 2,
                      "predictedAwayScore": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/admin/imports/seed")
                .contentType(APPLICATION_JSON)
                .content(payload)).andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/imports/seed")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCreated", is(0)))
                .andExpect(jsonPath("$.participantsSkipped", is(1)))
                .andExpect(jsonPath("$.teamsCreated", is(0)))
                .andExpect(jsonPath("$.teamsSkipped", is(2)))
                .andExpect(jsonPath("$.matchesCreated", is(0)))
                .andExpect(jsonPath("$.matchesSkipped", is(1)))
                .andExpect(jsonPath("$.predictionsCreated", is(0)))
                .andExpect(jsonPath("$.predictionsSkipped", is(1)));

        org.junit.jupiter.api.Assertions.assertEquals(1, participantRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(2, teamRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, matchRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, predictionRepository.count());
    }

    @Test
    void shouldRejectDuplicatePredictionInsidePayload() throws Exception {
        String payload = """
                {
                  "participants": [{"name": "Ana"}],
                  "teams": [
                    {"name": "Brasil", "fifaCode": "BRA"},
                    {"name": "Argentina", "fifaCode": "ARG"}
                  ],
                  "matches": [
                    {
                      "phase": "GROUP_STAGE",
                      "matchNumber": 1,
                      "homeTeamCode": "BRA",
                      "awayTeamCode": "ARG",
                      "startsAt": "2026-06-11T21:00:00Z",
                      "status": "SCHEDULED",
                      "venue": "Mexico City"
                    }
                  ],
                  "predictions": [
                    {
                      "participantName": "Ana",
                      "matchNumber": 1,
                      "predictedHomeScore": 2,
                      "predictedAwayScore": 1
                    },
                    {
                      "participantName": "Ana",
                      "matchNumber": 1,
                      "predictedHomeScore": 1,
                      "predictedAwayScore": 0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/admin/imports/seed")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Duplicate prediction in payload for participant Ana and match 1")));

        org.junit.jupiter.api.Assertions.assertEquals(0, participantRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, teamRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, matchRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, predictionRepository.count());
    }
}
