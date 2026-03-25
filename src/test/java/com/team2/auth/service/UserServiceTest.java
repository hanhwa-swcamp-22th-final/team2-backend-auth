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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
    }

    @Test
    @DisplayName("사용자를 생성할 수 있다")
    void createUser_success() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .password("rawPassword")
                .role(Role.SALES)
                .build();
        given(userRepository.existsByEmail("hong@test.com")).willReturn(false);
        given(userRepository.existsByEmployeeNo("EMP001")).willReturn(false);
        given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        User result = userService.createUser(request);

        // then
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("hong@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 사용자 생성 시 예외가 발생한다")
    void createUser_duplicateEmail() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .password("rawPassword")
                .role(Role.SALES)
                .build();
        given(userRepository.existsByEmail("hong@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    @DisplayName("중복 사번으로 사용자 생성 시 예외가 발생한다")
    void createUser_duplicateEmployeeNo() {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .password("rawPassword")
                .role(Role.SALES)
                .build();
        given(userRepository.existsByEmail("hong@test.com")).willReturn(false);
        given(userRepository.existsByEmployeeNo("EMP001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사번");
    }

    @Test
    @DisplayName("ID로 사용자를 조회할 수 있다")
    void getUser_success() {
        // given
        given(userRepository.findById(1)).willReturn(Optional.of(user));

        // when
        User result = userService.getUser(1);

        // then
        assertThat(result.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void getUser_notFound() {
        // given
        given(userRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다")
    void updateUser_success() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(1)
                .positionId(1)
                .build();
        Department department = new Department("영업부");
        Position position = new Position("팀원", 2);
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(departmentRepository.findById(1)).willReturn(Optional.of(department));
        given(positionRepository.findById(1)).willReturn(Optional.of(position));

        // when
        User result = userService.updateUser(1, request);

        // then
        assertThat(result.getName()).isEqualTo("김길동");
        assertThat(result.getEmail()).isEqualTo("kim@test.com");
    }

    @Test
    @DisplayName("사용자 상태를 변경할 수 있다")
    void changeStatus_success() {
        // given
        given(userRepository.findById(1)).willReturn(Optional.of(user));

        // when
        User result = userService.changeStatus(1, UserStatus.휴직);

        // then
        assertThat(result.getStatus()).isEqualTo(UserStatus.휴직);
    }

    @Test
    @DisplayName("퇴직 상태의 사용자는 상태를 변경할 수 없다")
    void changeStatus_retired() {
        // given
        User retiredUser = User.builder()
                .employeeNo("EMP002")
                .name("김철수")
                .email("kim@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.퇴직)
                .build();
        given(userRepository.findById(1)).willReturn(Optional.of(retiredUser));

        // when & then
        assertThatThrownBy(() -> userService.changeStatus(1, UserStatus.재직))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("퇴직한 사용자");
    }

    @Test
    @DisplayName("전체 사용자 목록을 조회할 수 있다")
    void getAllUsers() {
        // given
        given(userRepository.findAll()).willReturn(List.of(user));

        // when
        List<User> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("departmentId가 null이면 부서 배정을 건너뛴다")
    void updateUser_nullDepartmentId() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(null)
                .positionId(1)
                .build();
        Position position = new Position("팀원", 2);
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(positionRepository.findById(1)).willReturn(Optional.of(position));

        // when
        User result = userService.updateUser(1, request);

        // then
        assertThat(result.getName()).isEqualTo("김길동");
        assertThat(result.getEmail()).isEqualTo("kim@test.com");
    }

    @Test
    @DisplayName("positionId가 null이면 직급 배정을 건너뛴다")
    void updateUser_nullPositionId() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(1)
                .positionId(null)
                .build();
        Department department = new Department("영업부");
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(departmentRepository.findById(1)).willReturn(Optional.of(department));

        // when
        User result = userService.updateUser(1, request);

        // then
        assertThat(result.getName()).isEqualTo("김길동");
        assertThat(result.getEmail()).isEqualTo("kim@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 부서 ID로 수정 시 예외가 발생한다")
    void updateUser_departmentNotFound() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(999)
                .positionId(null)
                .build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(departmentRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부서를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 직급 ID로 수정 시 예외가 발생한다")
    void updateUser_positionNotFound() {
        // given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("김길동")
                .email("kim@test.com")
                .departmentId(null)
                .positionId(999)
                .build();
        given(userRepository.findById(1)).willReturn(Optional.of(user));
        given(positionRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(1, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다");
    }
}
