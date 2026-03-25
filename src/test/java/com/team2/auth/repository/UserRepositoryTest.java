package com.team2.auth.repository;

import com.team2.auth.entity.Department;
import com.team2.auth.entity.Position;
import com.team2.auth.entity.User;
import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = MybatisAutoConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    private Department savedDepartment;
    private User savedUser;

    @BeforeEach
    void setUp() {
        savedDepartment = departmentRepository.save(new Department("영업부"));
        Position position = positionRepository.save(new Position("팀원", 2));

        User user = User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
        user.assignDepartment(savedDepartment);
        user.assignPosition(position);
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmail() {
        // given
        String email = "hong@test.com";

        // when
        Optional<User> result = userRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByEmail_notFound() {
        // given
        String email = "notexist@test.com";

        // when
        Optional<User> result = userRepository.findByEmail(email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사번으로 사용자를 조회할 수 있다")
    void findByEmployeeNo() {
        // given
        String employeeNo = "EMP001";

        // when
        Optional<User> result = userRepository.findByEmployeeNo(employeeNo);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given & when
        boolean exists = userRepository.existsByEmail("hong@test.com");
        boolean notExists = userRepository.existsByEmail("notexist@test.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사번 존재 여부를 확인할 수 있다")
    void existsByEmployeeNo() {
        // given & when
        boolean exists = userRepository.existsByEmployeeNo("EMP001");
        boolean notExists = userRepository.existsByEmployeeNo("EMP999");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("부서 ID로 사용자 목록을 조회할 수 있다")
    void findByDepartmentId() {
        // given
        User user2 = User.builder()
                .employeeNo("EMP002")
                .name("김철수")
                .email("kim@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
        user2.assignDepartment(savedDepartment);
        userRepository.save(user2);

        Department otherDept = departmentRepository.save(new Department("생산부"));

        // when
        List<User> salesUsers = userRepository.findByDepartmentId(savedDepartment.getId());
        List<User> prodUsers = userRepository.findByDepartmentId(otherDept.getId());

        // then
        assertThat(salesUsers).hasSize(2);
        assertThat(prodUsers).isEmpty();
    }

    @Test
    @DisplayName("상태로 사용자 목록을 조회할 수 있다")
    void findByStatus() {
        // given
        User retiredUser = User.builder()
                .employeeNo("EMP003")
                .name("이영희")
                .email("lee@test.com")
                .pw("encodedPassword")
                .role(Role.SALES)
                .status(UserStatus.퇴직)
                .build();
        userRepository.save(retiredUser);

        // when
        List<User> activeUsers = userRepository.findByStatus(UserStatus.재직);
        List<User> retiredUsers = userRepository.findByStatus(UserStatus.퇴직);

        // then
        assertThat(activeUsers).hasSize(1);
        assertThat(retiredUsers).hasSize(1);
    }

    @Test
    @DisplayName("사용자 정보 수정 시 updatedAt이 갱신된다")
    void updateUser_updatesTimestamp() {
        // given
        savedUser.updateInfo("김길동", "kim@test.com");

        // when
        userRepository.saveAndFlush(savedUser);
        User updated = userRepository.findById(savedUser.getId()).orElseThrow();

        // then
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getName()).isEqualTo("김길동");
    }
}
