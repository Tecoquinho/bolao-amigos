package com.teco.bolao;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.teco.bolao.repository.MatchRepository;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import com.teco.bolao.repository.TeamRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupStageSeedImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @BeforeEach
    void cleanDatabase() {
        predictionRepository.deleteAll();
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        participantRepository.deleteAll();
    }

    @Test
    void shouldImportCanonicalGroupStageSeedThroughExistingEndpoint() throws Exception {
        String payload = StreamUtils.copyToString(
                new ClassPathResource("import/group-stage-seed.json").getInputStream(),
                StandardCharsets.UTF_8
        );

        String responseBody = mockMvc.perform(post("/api/admin/imports/seed")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCreated", is(0)))
                .andExpect(jsonPath("$.participantsSkipped", is(0)))
                .andExpect(jsonPath("$.teamsCreated", is(48)))
                .andExpect(jsonPath("$.teamsSkipped", is(0)))
                .andExpect(jsonPath("$.matchesCreated", is(72)))
                .andExpect(jsonPath("$.matchesSkipped", is(0)))
                .andExpect(jsonPath("$.predictionsCreated", is(0)))
                .andExpect(jsonPath("$.predictionsSkipped", is(0)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertEquals(0, participantRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(48, teamRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(72, matchRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, predictionRepository.count());

        System.out.println(responseBody);
        System.out.printf(
                "counts participants=%d teams=%d matches=%d predictions=%d%n",
                participantRepository.count(),
                teamRepository.count(),
                matchRepository.count(),
                predictionRepository.count()
        );
    }
}
