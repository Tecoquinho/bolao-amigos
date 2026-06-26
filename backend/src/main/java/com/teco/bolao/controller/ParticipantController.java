package com.teco.bolao.controller;

import com.teco.bolao.dto.ParticipantPredictionResponseDto;
import com.teco.bolao.dto.ParticipantResponseDto;
import com.teco.bolao.service.PublicSnapshotService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final PublicSnapshotService publicSnapshotService;

    public ParticipantController(PublicSnapshotService publicSnapshotService) {
        this.publicSnapshotService = publicSnapshotService;
    }

    @GetMapping
    public List<ParticipantResponseDto> getParticipants() {
        return publicSnapshotService.getParticipants();
    }

    @GetMapping("/{participantId}")
    public ParticipantResponseDto getParticipant(@PathVariable Long participantId) {
        return publicSnapshotService.getParticipant(participantId);
    }

    @GetMapping("/{participantId}/predictions")
    public List<ParticipantPredictionResponseDto> getPredictions(@PathVariable Long participantId) {
        return publicSnapshotService.getPredictionsByParticipant(participantId);
    }
}
