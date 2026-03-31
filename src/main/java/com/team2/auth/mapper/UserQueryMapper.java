package com.team2.auth.mapper;

import com.team2.auth.entity.User;
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
}
