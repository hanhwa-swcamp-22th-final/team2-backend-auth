package com.team2.auth.query.service;

import com.team2.auth.command.domain.entity.Team;
import com.team2.auth.command.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamRepository teamRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<Team> getTeamsByDepartment(Integer departmentId) {
        return teamRepository.findByDepartmentDepartmentId(departmentId);
    }

    public Team getTeam(Integer id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
    }
}
