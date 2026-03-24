package com.team2.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentTest {

    @Test
    @DisplayName("부서 생성 성공: 부서명이 정상 설정된다.")
    void createDepartment_Success() {
        // given & when
        Department department = new Department("영업1팀");

        // then
        assertEquals("영업1팀", department.getName());
    }
}
