package com.team2.auth.query.mapper;

import com.team2.auth.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentQueryMapper {

    Department findById(@Param("departmentId") Integer departmentId);

    List<Department> findAll();

    Department findByDepartmentName(@Param("departmentName") String departmentName);
}
