package com.teco.bolao.controller;

import com.teco.bolao.dto.ParticipantPredictionResponseDto;
import com.teco.bolao.dto.ParticipantResponseDto;
import com.teco.bolao.service.ParticipantService;
import com.teco.bolao.service.PredictionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    private final PredictionService predictionService;

    public ParticipantController(
            ParticipantService participantService,
            PredictionService predictionService
    ) {
        this.participantService = participantService;
        this.predictionService = predictionService;
    }

    @GetMapping
    public List<ParticipantResponseDto> getParticipants() {
        return participantService.getParticipants();
    }

    @GetMapping("/{participantId}")
    public ParticipantResponseDto getParticipant(@PathVariable Long participantId) {
        return participantService.getParticipant(participantId);
    }

    @GetMapping("/{participantId}/predictions")
    public List<ParticipantPredictionResponseDto> getPredictions(@PathVariable Long participantId) {
        participantService.getParticipant(participantId);
        return predictionService.getPredictionsByParticipant(participantId);
    }
}
