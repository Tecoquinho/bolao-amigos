package com.teco.bolao.service;

import com.teco.bolao.dto.ParticipantResponseDto;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.exception.NotFoundException;
import com.teco.bolao.repository.ParticipantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public List<ParticipantResponseDto> getParticipants() {
        return participantRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ParticipantResponseDto getParticipant(Long participantId) {
        return toResponse(getParticipantEntity(participantId));
    }

    public Participant getParticipantEntity(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Participant not found: " + participantId));
    }

    private ParticipantResponseDto toResponse(Participant participant) {
        return new ParticipantResponseDto(participant.getId(), participant.getName());
    }
}
