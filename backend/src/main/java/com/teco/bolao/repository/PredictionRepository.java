package com.teco.bolao.repository;

import com.teco.bolao.entity.Prediction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    @EntityGraph(attributePaths = {"match", "match.homeTeam", "match.awayTeam"})
    List<Prediction> findByParticipantIdOrderByMatchStartsAtAsc(Long participantId);

    @EntityGraph(attributePaths = {"participant", "match", "match.homeTeam", "match.awayTeam"})
    List<Prediction> findByMatchId(Long matchId);

    @EntityGraph(attributePaths = {"participant", "match", "match.homeTeam", "match.awayTeam"})
    Optional<Prediction> findByParticipantIdAndMatchId(Long participantId, Long matchId);

    @Override
    @EntityGraph(attributePaths = {"participant", "match", "match.homeTeam", "match.awayTeam"})
    List<Prediction> findAll();
}
