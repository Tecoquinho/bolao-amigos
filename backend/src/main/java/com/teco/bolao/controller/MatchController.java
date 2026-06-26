package com.teco.bolao.controller;

import com.teco.bolao.dto.MatchResponseDto;
import com.teco.bolao.dto.MatchResultUpdateRequestDto;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import com.teco.bolao.service.MatchResultService;
import com.teco.bolao.service.PublicSnapshotService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final PublicSnapshotService publicSnapshotService;
    private final MatchResultService matchResultService;

    public MatchController(
            PublicSnapshotService publicSnapshotService,
            MatchResultService matchResultService
    ) {
        this.publicSnapshotService = publicSnapshotService;
        this.matchResultService = matchResultService;
    }

    @GetMapping
    public List<MatchResponseDto> getMatches(
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) TournamentPhase phase,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return publicSnapshotService.getMatches(status, phase, date);
    }

    @GetMapping("/{matchId}")
    public MatchResponseDto getMatch(@PathVariable Long matchId) {
        return publicSnapshotService.getMatch(matchId);
    }

    @PatchMapping("/{matchId}/result")
    public MatchResponseDto updateOfficialResult(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchResultUpdateRequestDto request
    ) {
        return matchResultService.updateOfficialResult(matchId, request.homeScore(), request.awayScore());
    }
}
