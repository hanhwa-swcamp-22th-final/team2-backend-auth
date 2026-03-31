package com.team2.auth.repository;

import com.team2.auth.command.repository.DepartmentRepository;
import com.team2.auth.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = MybatisAutoConfiguration.class)
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.save(new Department("영업부"));
        departmentRepository.save(new Department("생산부"));
    }

    @Test
    @DisplayName("부서명으로 부서를 조회할 수 있다")
    void findByDepartmentName() {
        // given
        String name = "영업부";

        // when
        Optional<Department> result = departmentRepository.findByDepartmentName(name);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDepartmentName()).isEqualTo("영업부");
    }

    @Test
    @DisplayName("부서 저장 시 createdAt이 자동 설정된다")
    void saveDepartment_setsCreatedAt() {
        // given
        Department dept = new Department("인사부");

        // when
        Department saved = departmentRepository.saveAndFlush(dept);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 부서명으로 조회하면 빈 Optional을 반환한다")
    void findByDepartmentName_notFound() {
        // given
        String name = "인사부";

        // when
        Optional<Department> result = departmentRepository.findByDepartmentName(name);

        // then
        assertThat(result).isEmpty();
    }
}
