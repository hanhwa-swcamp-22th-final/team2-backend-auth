package com.team2.auth.query.mapper;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.query.dto.UserListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserQueryMapper {

    User findById(@Param("userId") Integer userId);

    User findByUserEmail(@Param("userEmail") String userEmail);

    User findByEmployeeNo(@Param("employeeNo") String employeeNo);

    List<User> findAll();

    List<User> findByTeamId(@Param("teamId") Integer teamId);

    List<User> findByDepartmentId(@Param("departmentId") Integer departmentId);

    List<User> findByUserStatus(@Param("userStatus") String userStatus);

    int existsByUserEmail(@Param("userEmail") String userEmail);

    int existsByEmployeeNo(@Param("employeeNo") String employeeNo);

    List<UserListResponse> findByCondition(@Param("userName") String userName,
                                           @Param("teamId") Integer teamId,
                                           @Param("departmentId") Integer departmentId,
                                           @Param("userRole") String userRole,
                                           @Param("userStatus") String userStatus,
                                           @Param("size") int size,
                                           @Param("offset") int offset);

    long countByCondition(@Param("userName") String userName,
                          @Param("teamId") Integer teamId,
                          @Param("departmentId") Integer departmentId,
                          @Param("userRole") String userRole,
                          @Param("userStatus") String userStatus);

    /**
     * 결재자 후보 조회.
     * - teamId 지정 시: 해당 팀의 팀장(position_level=1) + 전체 ADMIN 사용자
     * - teamId null: 전체 팀의 팀장 + 전체 ADMIN
     * active 상태만 반환.
     */
    List<UserListResponse> findApprovers(@Param("teamId") Integer teamId);

    /**
     * 팀 소속 active 사용자 ID 만 반환. Documents 서비스가 PI/PO 팀 스코프 필터에 사용.
     */
    List<Integer> findUserIdsByTeam(@Param("teamId") Integer teamId);
}
