package com.team2.auth.service;

import com.team2.auth.command.service.DepartmentCommandService;
import com.team2.auth.query.service.DepartmentQueryService;
import com.team2.auth.entity.Department;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.command.repository.DepartmentRepository;
import com.team2.auth.command.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class DepartmentServiceTest {

    @Autowired
    private DepartmentCommandService departmentCommandService;

    @Autowired
    private DepartmentQueryService departmentQueryService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("부서를 생성할 수 있다")
    void createDepartment_success() {
        // when
        Department result = departmentCommandService.createDepartment("영업부");

        // then
        assertThat(result.getDepartmentId()).isNotNull();
        assertThat(result.getDepartmentName()).isEqualTo("영업부");

        // DB에 실제 저장 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(departmentRepository.findByDepartmentName("영업부")).isPresent();
    }

    @Test
    @DisplayName("전체 부서 목록을 조회할 수 있다")
    void getAllDepartments() {
        // given
        departmentCommandService.createDepartment("영업부");
        departmentCommandService.createDepartment("생산부");
        entityManager.flush();
        entityManager.clear();

        // when
        List<Department> result = departmentRepository.findAll();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("소속 사용자가 없는 부서를 삭제할 수 있다")
    void deleteDepartment_success() {
        // given
        Department dept = departmentCommandService.createDepartment("영업부");
        entityManager.flush();
        entityManager.clear();

        // when
        departmentCommandService.deleteDepartment(dept.getDepartmentId());
        entityManager.flush();
        entityManager.clear();

        // then - DB에서 삭제 확인
        assertThat(departmentRepository.findById(dept.getDepartmentId())).isEmpty();
    }

    @Test
    @DisplayName("소속 사용자가 있는 부서는 삭제할 수 없다")
    void deleteDepartment_hasUsers() {
        // given - 부서 생성 후 사용자 배정
        Department dept = departmentRepository.saveAndFlush(new Department("영업부"));
        User user = User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw(passwordEncoder.encode("pw"))
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        user.assignDepartment(dept);
        userRepository.saveAndFlush(user);
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> departmentCommandService.deleteDepartment(dept.getDepartmentId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("소속된 사용자가 있어 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 부서 삭제 시 예외가 발생한다")
    void deleteDepartment_notFound() {
        assertThatThrownBy(() -> departmentCommandService.deleteDepartment(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("ID로 부서를 조회할 수 있다")
    void getDepartment_success() {
        Department saved = departmentCommandService.createDepartment("영업부");
        entityManager.flush();
        entityManager.clear();

        Department result = departmentQueryService.getDepartment(saved.getDepartmentId());

        assertThat(result.getDepartmentName()).isEqualTo("영업부");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 부서 조회 시 예외가 발생한다")
    void getDepartment_notFound() {
        assertThatThrownBy(() -> departmentQueryService.getDepartment(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이름으로 부서를 조회할 수 있다")
    void getDepartmentByName_success() {
        departmentCommandService.createDepartment("영업부");
        entityManager.flush();
        entityManager.clear();

        Department result = departmentQueryService.getDepartmentByName("영업부");

        assertThat(result.getDepartmentName()).isEqualTo("영업부");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 부서 조회 시 예외가 발생한다")
    void getDepartmentByName_notFound() {
        assertThatThrownBy(() -> departmentQueryService.getDepartmentByName("없는부서"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }
}
