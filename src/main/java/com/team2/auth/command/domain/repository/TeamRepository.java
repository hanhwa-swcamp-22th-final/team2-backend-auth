package com.team2.auth.command.domain.repository;

import com.team2.auth.command.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {

    List<Team> findByDepartmentDepartmentId(Integer departmentId);

    Optional<Team> findByDepartmentDepartmentIdAndTeamName(Integer departmentId, String teamName);

    long countByDepartmentDepartmentId(Integer departmentId);
}
