package com.team2.auth.service;

import com.team2.auth.entity.Department;
import com.team2.auth.repository.DepartmentRepository;
import com.team2.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    @DisplayName("부서를 생성할 수 있다")
    void createDepartment_success() {
        // given
        Department department = new Department("영업부");
        given(departmentRepository.save(any(Department.class))).willReturn(department);

        // when
        Department result = departmentService.createDepartment("영업부");

        // then
        assertThat(result.getName()).isEqualTo("영업부");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("전체 부서 목록을 조회할 수 있다")
    void getAllDepartments() {
        // given
        given(departmentRepository.findAll()).willReturn(List.of(
                new Department("영업부"),
                new Department("생산부")
        ));

        // when
        List<Department> result = departmentService.getAllDepartments();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("소속 사용자가 없는 부서를 삭제할 수 있다")
    void deleteDepartment_success() {
        // given
        Department department = new Department("영업부");
        given(departmentRepository.findById(1)).willReturn(Optional.of(department));
        given(userRepository.findByDepartmentId(1)).willReturn(Collections.emptyList());

        // when
        departmentService.deleteDepartment(1);

        // then
        verify(departmentRepository).delete(department);
    }

    @Test
    @DisplayName("소속 사용자가 있는 부서는 삭제할 수 없다")
    void deleteDepartment_hasUsers() {
        // given
        Department department = new Department("영업부");
        given(departmentRepository.findById(1)).willReturn(Optional.of(department));
        given(userRepository.findByDepartmentId(1)).willReturn(List.of(
                com.team2.auth.entity.User.builder()
                        .employeeNo("EMP001")
                        .name("홍길동")
                        .email("hong@test.com")
                        .pw("pw")
                        .role(com.team2.auth.entity.enums.Role.SALES)
                        .status(com.team2.auth.entity.enums.UserStatus.재직)
                        .build()
        ));

        // when & then
        assertThatThrownBy(() -> departmentService.deleteDepartment(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("소속된 사용자가 있어 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 부서 삭제 시 예외가 발생한다")
    void deleteDepartment_notFound() {
        // given
        given(departmentRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> departmentService.deleteDepartment(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }
}
