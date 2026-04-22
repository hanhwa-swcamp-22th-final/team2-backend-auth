package com.team2.auth.command.domain.repository;

import com.team2.auth.command.domain.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Integer> {

    Optional<Position> findByPositionName(String positionName);

    Optional<Position> findFirstByPositionNameOrderByPositionIdAsc(String positionName);
}
