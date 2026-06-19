package com.teco.bolao.repository;

import com.teco.bolao.entity.Participant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findAllByOrderByNameAsc();

    Optional<Participant> findByName(String name);
}
