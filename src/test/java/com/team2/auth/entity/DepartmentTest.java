package com.team2.auth.entity;

import com.team2.auth.command.repository.DepartmentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
class DepartmentTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("부서 생성 성공: 부서명이 정상 설정된다.")
    void createDepartment_Success() {
        // given & when
        Department department = new Department("영업1팀");

        // then - 도메인 로직 검증
        assertEquals("영업1팀", department.getDepartmentName());

        // DB 저장 후 재조회 검증
        departmentRepository.save(department);
        entityManager.flush();
        entityManager.clear();

        Department found = departmentRepository.findById(department.getDepartmentId()).orElseThrow();
        assertEquals("영업1팀", found.getDepartmentName());
        // @PrePersist로 createdAt 자동설정 확인
        assertNotNull(found.getCreatedAt());
    }
}
