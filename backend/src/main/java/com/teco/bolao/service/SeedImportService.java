package com.teco.bolao.service;

import com.teco.bolao.dto.SeedImportRequestDto;
import com.teco.bolao.dto.SeedImportResultDto;
import com.teco.bolao.dto.SeedMatchDto;
import com.teco.bolao.dto.SeedParticipantDto;
import com.teco.bolao.dto.SeedPredictionDto;
import com.teco.bolao.dto.SeedTeamDto;
import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.Participant;
import com.teco.bolao.entity.Prediction;
import com.teco.bolao.entity.Team;
import com.teco.bolao.exception.BadRequestException;
import com.teco.bolao.repository.MatchRepository;
import com.teco.bolao.repository.ParticipantRepository;
import com.teco.bolao.repository.PredictionRepository;
import com.teco.bolao.repository.TeamRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedImportService {

    private final ParticipantRepository participantRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final ScoreCalculationService scoreCalculationService;

    public SeedImportService(
            ParticipantRepository participantRepository,
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            PredictionRepository predictionRepository,
            ScoreCalculationService scoreCalculationService
    ) {
        this.participantRepository = participantRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.scoreCalculationService = scoreCalculationService;
    }

    @Transactional
    public SeedImportResultDto importSeed(SeedImportRequestDto request) {
        validatePayload(request);

        int participantsCreated = 0;
        int participantsSkipped = 0;
        int teamsCreated = 0;
        int teamsSkipped = 0;
        int matchesCreated = 0;
        int matchesSkipped = 0;
        int predictionsCreated = 0;
        int predictionsSkipped = 0;

        for (SeedParticipantDto participantDto : request.participants()) {
            if (participantRepository.findByName(participantDto.name().trim()).isPresent()) {
                participantsSkipped++;
                continue;
            }

            participantRepository.save(new Participant(participantDto.name().trim()));
            participantsCreated++;
        }

        for (SeedTeamDto teamDto : request.teams()) {
            String fifaCode = normalizeCode(teamDto.fifaCode());
            if (teamRepository.findByFifaCode(fifaCode).isPresent()) {
                teamsSkipped++;
                continue;
            }

            teamRepository.save(new Team(teamDto.name().trim(), fifaCode, teamDto.flagUrl()));
            teamsCreated++;
        }

        for (SeedMatchDto matchDto : request.matches()) {
            if (matchRepository.findByMatchNumber(matchDto.matchNumber()).isPresent()) {
                matchesSkipped++;
                continue;
            }

            Team homeTeam = teamRepository.findByFifaCode(normalizeCode(matchDto.homeTeamCode()))
                    .orElseThrow(() -> new BadRequestException("Home team not found for code: " + matchDto.homeTeamCode()));
            Team awayTeam = teamRepository.findByFifaCode(normalizeCode(matchDto.awayTeamCode()))
                    .orElseThrow(() -> new BadRequestException("Away team not found for code: " + matchDto.awayTeamCode()));

            Match match = new Match(
                    matchDto.phase(),
                    matchDto.matchNumber(),
                    homeTeam,
                    awayTeam,
                    matchDto.startsAt(),
                    matchDto.status(),
                    matchDto.venue()
            );

            if (matchDto.status() == MatchStatus.FINISHED) {
                if (matchDto.homeScore() == null || matchDto.awayScore() == null) {
                    throw new BadRequestException("Finished match must define homeScore and awayScore: " + matchDto.matchNumber());
                }
                match.updateOfficialResult(matchDto.homeScore(), matchDto.awayScore(), OffsetDateTime.now());
            } else if (matchDto.homeScore() != null || matchDto.awayScore() != null) {
                throw new BadRequestException("Only finished matches can define official score: " + matchDto.matchNumber());
            }

            matchRepository.save(match);
            matchesCreated++;
        }

        for (SeedPredictionDto predictionDto : request.predictions()) {
            Participant participant = participantRepository.findByName(predictionDto.participantName().trim())
                    .orElseThrow(() -> new BadRequestException("Participant not found: " + predictionDto.participantName()));
            Match match = matchRepository.findByMatchNumber(predictionDto.matchNumber())
                    .orElseThrow(() -> new BadRequestException("Match not found: " + predictionDto.matchNumber()));

            if (predictionRepository.findByParticipantIdAndMatchId(participant.getId(), match.getId()).isPresent()) {
                predictionsSkipped++;
                continue;
            }

            Prediction prediction = new Prediction(
                    participant,
                    match,
                    predictionDto.predictedHomeScore(),
                    predictionDto.predictedAwayScore()
            );

            if (match.getStatus() == MatchStatus.FINISHED && match.getHomeScore() != null && match.getAwayScore() != null) {
                prediction.updatePointsAwarded(scoreCalculationService.calculatePoints(
                        match.getHomeScore(),
                        match.getAwayScore(),
                        predictionDto.predictedHomeScore(),
                        predictionDto.predictedAwayScore()
                ));
            }

            predictionRepository.save(prediction);
            predictionsCreated++;
        }

        return new SeedImportResultDto(
                participantsCreated,
                participantsSkipped,
                teamsCreated,
                teamsSkipped,
                matchesCreated,
                matchesSkipped,
                predictionsCreated,
                predictionsSkipped
        );
    }

    private void validatePayload(SeedImportRequestDto request) {
        validateUniqueParticipants(request.participants());
        validateUniqueTeams(request.teams());
        validateUniqueMatches(request.matches());
        validateUniquePredictions(request.predictions());
        validateMatchTeams(request.matches());
    }

    private void validateUniqueParticipants(List<SeedParticipantDto> participants) {
        Set<String> names = new HashSet<>();
        for (SeedParticipantDto participant : participants) {
            String normalized = participant.name().trim().toLowerCase(Locale.ROOT);
            if (!names.add(normalized)) {
                throw new BadRequestException("Duplicate participant in payload: " + participant.name());
            }
        }
    }

    private void validateUniqueTeams(List<SeedTeamDto> teams) {
        Set<String> codes = new HashSet<>();
        for (SeedTeamDto team : teams) {
            String normalized = normalizeCode(team.fifaCode());
            if (!codes.add(normalized)) {
                throw new BadRequestException("Duplicate team in payload: " + team.fifaCode());
            }
        }
    }

    private void validateUniqueMatches(List<SeedMatchDto> matches) {
        Set<Integer> numbers = new HashSet<>();
        for (SeedMatchDto match : matches) {
            if (!numbers.add(match.matchNumber())) {
                throw new BadRequestException("Duplicate matchNumber in payload: " + match.matchNumber());
            }
        }
    }

    private void validateUniquePredictions(List<SeedPredictionDto> predictions) {
        Set<String> keys = new HashSet<>();
        for (SeedPredictionDto prediction : predictions) {
            String key = prediction.participantName().trim().toLowerCase(Locale.ROOT) + "::" + prediction.matchNumber();
            if (!keys.add(key)) {
                throw new BadRequestException(
                        "Duplicate prediction in payload for participant " + prediction.participantName()
                                + " and match " + prediction.matchNumber()
                );
            }
        }
    }

    private void validateMatchTeams(List<SeedMatchDto> matches) {
        for (SeedMatchDto match : matches) {
            if (normalizeCode(match.homeTeamCode()).equals(normalizeCode(match.awayTeamCode()))) {
                throw new BadRequestException("Match cannot have the same team on both sides: " + match.matchNumber());
            }
        }
    }

    private String normalizeCode(String fifaCode) {
        return fifaCode.trim().toUpperCase(Locale.ROOT);
    }
}
