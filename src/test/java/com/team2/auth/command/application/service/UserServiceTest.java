package com.team2.auth.service;

import com.team2.auth.command.application.service.EmailService;
import com.team2.auth.command.application.service.UserCommandService;
import com.team2.auth.query.service.UserQueryService;
import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.DepartmentRepository;
import com.team2.auth.command.domain.repository.PositionRepository;
import com.team2.auth.command.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserListResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class UserServiceTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private UserQueryService userQueryService;

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
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw(passwordEncoder.encode("rawPassword"))
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
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
        User result = userCommandService.createUser(request);

        // then
        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getUserName()).isEqualTo("김철수");
        assertThat(result.getUserEmail()).isEqualTo("kim@test.com");
        assertThat(result.getUserStatus()).isEqualTo(UserStatus.ACTIVE);

        // BCrypt로 암호화되어 저장됐는지 확인
        assertThat(passwordEncoder.matches("password123", result.getUserPw())).isTrue();

        // DB에 실제로 저장됐는지 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.findByUserEmail("kim@test.com")).isPresent();
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

        assertThatThrownBy(() -> userCommandService.createUser(request))
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

        assertThatThrownBy(() -> userCommandService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사번");
    }

    @Test
    @DisplayName("ID로 사용자를 조회할 수 있다")
    void getUser_success() {
        User result = userRepository.findById(savedUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        assertThat(result.getUserName()).isEqualTo("홍길동");
        assertThat(result.getUserEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getUser_notFound() {
        assertThat(userRepository.findById(99999)).isEmpty();
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다 (부서/직급 포함)")
    void updateUser_withDeptAndPosition() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(savedDept.getDepartmentId())
                .positionId(savedPosition.getPositionId())
                .build();

        // when
        User result = userCommandService.updateUser(savedUser.getUserId(), request);
        entityManager.flush();
        entityManager.clear();

        // then - DB에서 다시 조회해서 실제 반영 확인
        User updated = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(updated.getUserName()).isEqualTo("김길동");
        assertThat(updated.getUserEmail()).isEqualTo("kim@test.com");
        assertThat(updated.getDepartment().getDepartmentName()).isEqualTo("영업부");
        assertThat(updated.getPosition().getPositionName()).isEqualTo("팀원");
    }

    @Test
    @DisplayName("departmentId가 null이면 부서 배정을 건너뛴다")
    void updateUser_nullDepartmentId() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(null)
                .positionId(savedPosition.getPositionId())
                .build();

        User result = userCommandService.updateUser(savedUser.getUserId(), request);

        assertThat(result.getUserName()).isEqualTo("김길동");
        assertThat(result.getDepartment()).isNull();
    }

    @Test
    @DisplayName("positionId가 null이면 직급 배정을 건너뛴다")
    void updateUser_nullPositionId() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(savedDept.getDepartmentId())
                .positionId(null)
                .build();

        User result = userCommandService.updateUser(savedUser.getUserId(), request);

        assertThat(result.getUserName()).isEqualTo("김길동");
        assertThat(result.getPosition()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 부서 ID로 수정 시 예외가 발생한다")
    void updateUser_departmentNotFound() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .departmentId(99999)
                .build();

        assertThatThrownBy(() -> userCommandService.updateUser(savedUser.getUserId(), request))
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

        assertThatThrownBy(() -> userCommandService.updateUser(savedUser.getUserId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 상태를 변경할 수 있다")
    void changeStatus_success() {
        User result = userCommandService.changeStatus(savedUser.getUserId(), UserStatus.ON_LEAVE);

        assertThat(result.getUserStatus()).isEqualTo(UserStatus.ON_LEAVE);

        // DB에서도 확인
        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(updated.getUserStatus()).isEqualTo(UserStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("퇴직 상태의 사용자는 상태를 변경할 수 없다")
    void changeStatus_retired() {
        // 먼저 퇴직 상태로 변경
        userCommandService.changeStatus(savedUser.getUserId(), UserStatus.RETIRED);
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> userCommandService.changeStatus(savedUser.getUserId(), UserStatus.ACTIVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("퇴직한 사용자");
    }

    @Test
    @DisplayName("전체 사용자 목록을 조회할 수 있다")
    void getAllUsers() {
        List<User> result = userRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("상태별 사용자 목록을 조회할 수 있다")
    void getUsersByStatus_success() {
        entityManager.flush();
        entityManager.clear();

        List<User> activeUsers = userQueryService.getUsersByStatus(UserStatus.ACTIVE);

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword_success() {
        // when
        userCommandService.changePassword(savedUser.getUserId(), "rawPassword", "newPassword");
        entityManager.flush();
        entityManager.clear();

        // then
        User updated = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword", updated.getUserPw())).isTrue();
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
    void changePassword_wrongCurrentPassword() {
        assertThatThrownBy(() -> userCommandService.changePassword(savedUser.getUserId(), "wrongPassword", "newPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("비밀번호를 초기화할 수 있다")
    void resetPassword_success() {
        // when
        userCommandService.resetPassword(savedUser.getUserId());
        entityManager.flush();
        entityManager.clear();

        // then
        User updated = userRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(passwordEncoder.matches("test1234", updated.getUserPw())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 찾기 시 임시 비밀번호를 이메일로 발송한다")
    void forgotPassword_success() {
        // given
        entityManager.flush();
        entityManager.clear();

        // when
        userCommandService.forgotPassword("hong@test.com");

        // then
        verify(emailService).sendTemporaryPassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("등록되지 않은 이메일로 비밀번호 찾기 시 예외가 발생한다")
    void forgotPassword_unknownEmail() {
        assertThatThrownBy(() -> userCommandService.forgotPassword("unknown@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("등록되지 않은 이메일");
    }

    @Test
    @DisplayName("사용자 목록을 페이징하여 조회할 수 있다")
    void getUsers_paged() {
        // given
        entityManager.flush();
        entityManager.clear();

        // when
        PagedResponse<UserListResponse> result = userQueryService.getUsers(null, null, null, null, 0, 10);

        // then
        assertThat(result.content()).isNotEmpty();
        assertThat(result.totalElements()).isGreaterThan(0);
        assertThat(result.currentPage()).isEqualTo(0);
    }
}
