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

    List<User> findByDepartmentId(@Param("departmentId") Integer departmentId);

    List<User> findByUserStatus(@Param("userStatus") String userStatus);

    int existsByUserEmail(@Param("userEmail") String userEmail);

    int existsByEmployeeNo(@Param("employeeNo") String employeeNo);

    List<UserListResponse> findByCondition(@Param("userName") String userName,
                                           @Param("departmentId") Integer departmentId,
                                           @Param("userRole") String userRole,
                                           @Param("userStatus") String userStatus,
                                           @Param("size") int size,
                                           @Param("offset") int offset);

    long countByCondition(@Param("userName") String userName,
                          @Param("departmentId") Integer departmentId,
                          @Param("userRole") String userRole,
                          @Param("userStatus") String userStatus);
}
