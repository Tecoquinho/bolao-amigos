package com.teco.bolao.repository;

import com.teco.bolao.entity.Match;
import com.teco.bolao.entity.MatchStatus;
import com.teco.bolao.entity.TournamentPhase;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    List<Match> findAllByOrderByStartsAtAsc();

    @Override
    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    List<Match> findAll();

    @Override
    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    Optional<Match> findById(Long id);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    Optional<Match> findByMatchNumber(Integer matchNumber);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    List<Match> findByStatusOrderByStartsAtAsc(MatchStatus status);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    List<Match> findByPhaseOrderByStartsAtAsc(TournamentPhase phase);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    List<Match> findByStartsAtBetweenOrderByStartsAtAsc(OffsetDateTime startsAtStart, OffsetDateTime startsAtEnd);
}
