package com.team2.auth.service;

import com.team2.auth.dto.CreateUserRequest;
import com.team2.auth.dto.UpdateUserRequest;
import com.team2.auth.entity.Department;
import com.team2.auth.entity.Position;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import com.team2.auth.repository.DepartmentRepository;
import com.team2.auth.repository.PositionRepository;
import com.team2.auth.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private User savedUser;
    private Department savedDept;
    private Position savedPosition;

    @BeforeEach
    void setUp() {
        savedDept = departmentRepository.saveAndFlush(new Department("영업부"));
        savedPosition = positionRepository.saveAndFlush(new Position("팀원", 2));

        User user = User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw(passwordEncoder.encode("rawPassword"))
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
        savedUser = userRepository.saveAndFlush(user);
        entityManager.clear();
    }

    @Test
    @DisplayName("사용자를 생성할 수 있다")
    void createUser_success() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP002")
                .name("김철수")
                .email("kim@test.com")
                .password("password123")
                .role(Role.SALES)
                .build();

        // when
        User result = userService.createUser(request);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("김철수");
        assertThat(result.getEmail()).isEqualTo("kim@test.com");
        assertThat(result.getStatus()).isEqualTo(UserStatus.재직);

        // BCrypt로 암호화되어 저장됐는지 확인
        assertThat(passwordEncoder.matches("password123", result.getPw())).isTrue();

        // DB에 실제로 저장됐는지 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.findByEmail("kim@test.com")).isPresent();
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 시 예외가 발생한다")
    void createUser_duplicateEmail() {
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP002")
                .name("김철수")
                .email("hong@test.com")  // 이미 존재하는 이메일
                .password("password123")
                .role(Role.SALES)
                .build();

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    @DisplayName("중복 사번으로 사용자 생성 시 예외가 발생한다")
    void createUser_duplicateEmployeeNo() {
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP001")  // 이미 존재하는 사번
                .name("김철수")
                .email("kim@test.com")
                .password("password123")
                .role(Role.SALES)
                .build();

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사번");
    }

    @Test
    @DisplayName("ID로 사용자를 조회할 수 있다")
    void getUser_success() {
        User result = userService.getUser(savedUser.getId());

        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getUser_notFound() {
        assertThatThrownBy(() -> userService.getUser(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다 (부서/직급 포함)")
    void updateUser_withDeptAndPosition() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(savedDept.getId())
                .positionId(savedPosition.getId())
                .build();

        // when
        User result = userService.updateUser(savedUser.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then - DB에서 다시 조회해서 실제 반영 확인
        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("김길동");
        assertThat(updated.getEmail()).isEqualTo("kim@test.com");
        assertThat(updated.getDepartment().getName()).isEqualTo("영업부");
        assertThat(updated.getPosition().getName()).isEqualTo("팀원");
    }

    @Test
    @DisplayName("departmentId가 null이면 부서 배정을 건너뛴다")
    void updateUser_nullDepartmentId() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(null)
                .positionId(savedPosition.getId())
                .build();

        User result = userService.updateUser(savedUser.getId(), request);

        assertThat(result.getName()).isEqualTo("김길동");
        assertThat(result.getDepartment()).isNull();
    }

    @Test
    @DisplayName("positionId가 null이면 직급 배정을 건너뛴다")
    void updateUser_nullPositionId() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(savedDept.getId())
                .positionId(null)
                .build();

        User result = userService.updateUser(savedUser.getId(), request);

        assertThat(result.getName()).isEqualTo("김길동");
        assertThat(result.getPosition()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 부서 ID로 수정 시 예외가 발생한다")
    void updateUser_departmentNotFound() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .departmentId(99999)
                .build();

        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 직급 ID로 수정 시 예외가 발생한다")
    void updateUser_positionNotFound() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .positionId(99999)
                .build();

        assertThatThrownBy(() -> userService.updateUser(savedUser.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 상태를 변경할 수 있다")
    void changeStatus_success() {
        User result = userService.changeStatus(savedUser.getId(), UserStatus.휴직);

        assertThat(result.getStatus()).isEqualTo(UserStatus.휴직);

        // DB에서도 확인
        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(UserStatus.휴직);
    }

    @Test
    @DisplayName("퇴직 상태의 사용자는 상태를 변경할 수 없다")
    void changeStatus_retired() {
        // 먼저 퇴직 상태로 변경
        userService.changeStatus(savedUser.getId(), UserStatus.퇴직);
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> userService.changeStatus(savedUser.getId(), UserStatus.재직))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("퇴직한 사용자");
    }

    @Test
    @DisplayName("전체 사용자 목록을 조회할 수 있다")
    void getAllUsers() {
        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("홍길동");
    }
}
