package com.teco.bolao.repository;

import com.teco.bolao.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByFifaCode(String fifaCode);
}
