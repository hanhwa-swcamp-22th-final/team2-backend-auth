package com.team2.auth.repository;

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
    void findByName() {
        // given
        String name = "영업부";

        // when
        Optional<Department> result = departmentRepository.findByName(name);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("영업부");
    }

    @Test
    @DisplayName("존재하지 않는 부서명으로 조회하면 빈 Optional을 반환한다")
    void findByName_notFound() {
        // given
        String name = "인사부";

        // when
        Optional<Department> result = departmentRepository.findByName(name);

        // then
        assertThat(result).isEmpty();
    }
}
