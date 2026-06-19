package com.teco.bolao.service;

import com.teco.bolao.dto.MatchResponseDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import com.teco.bolao.exception.NotFoundException;
import com.teco.bolao.repository.MatchRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<MatchResponseDto> getMatches(MatchStatus status, TournamentPhase phase, LocalDate date) {
        return matchRepository.findAllByOrderByStartsAtAsc()
                .stream()
                .filter(match -> status == null || match.getStatus() == status)
                .filter(match -> phase == null || match.getPhase() == phase)
                .filter(match -> date == null || match.getStartsAt().toLocalDate().isEqual(date))
                .sorted(Comparator.comparing(Match::getStartsAt).thenComparing(Match::getMatchNumber))
                .map(this::toResponse)
                .toList();
    }

    public MatchResponseDto getMatch(Long matchId) {
        return toResponse(getMatchEntity(matchId));
    }

    public Match getMatchEntity(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found: " + matchId));
    }

    public MatchResponseDto toResponse(Match match) {
        return new MatchResponseDto(
                match.getId(),
                match.getPhase(),
                match.getMatchNumber(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getFifaCode(),
                match.getHomeTeam().getFlagUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getFifaCode(),
                match.getAwayTeam().getFlagUrl(),
                match.getStartsAt(),
                match.getStatus(),
                match.getVenue(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getOfficialResultAt()
        );
    }
}
