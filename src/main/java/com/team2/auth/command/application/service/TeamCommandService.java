package com.team2.auth.command.application.service;

import com.team2.auth.command.application.dto.CreateTeamRequest;
import com.team2.auth.command.application.dto.UpdateTeamRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.domain.entity.Team;
import com.team2.auth.command.domain.repository.DepartmentRepository;
import com.team2.auth.command.domain.repository.TeamRepository;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamCommandService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final UserQueryService userQueryService;

    public Team createTeam(CreateTeamRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        teamRepository.findByDepartmentDepartmentIdAndTeamName(department.getDepartmentId(), request.getTeamName())
                .ifPresent(t -> { throw new IllegalArgumentException("이미 같은 이름의 팀이 해당 부서에 존재합니다."); });
        return teamRepository.save(new Team(request.getTeamName(), department));
    }

    public Team updateTeam(Integer id, UpdateTeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        if (request.getTeamName() != null && !request.getTeamName().isBlank()) {
            team.updateName(request.getTeamName());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
            team.changeDepartment(department);
        }
        return team;
    }

    public void deleteTeam(Integer id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        if (!userQueryService.getUsersByTeam(id).isEmpty()) {
            throw new IllegalStateException("소속된 사용자가 있어 삭제할 수 없습니다.");
        }
        teamRepository.delete(team);
    }
}
