package com.team2.auth.repository;

import com.team2.auth.command.repository.DepartmentRepository;
import com.team2.auth.command.repository.PositionRepository;
import com.team2.auth.command.repository.UserRepository;
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
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        user.assignDepartment(savedDepartment);
        user.assignPosition(position);
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByUserEmail() {
        // given
        String email = "hong@test.com";

        // when
        Optional<User> result = userRepository.findByUserEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByUserEmail_notFound() {
        // given
        String email = "notexist@test.com";

        // when
        Optional<User> result = userRepository.findByUserEmail(email);

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
        assertThat(result.get().getUserEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByUserEmail() {
        // given & when
        boolean exists = userRepository.existsByUserEmail("hong@test.com");
        boolean notExists = userRepository.existsByUserEmail("notexist@test.com");

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
    void findByDepartmentDepartmentId() {
        // given
        User user2 = User.builder()
                .employeeNo("EMP002")
                .userName("김철수")
                .userEmail("kim@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
        user2.assignDepartment(savedDepartment);
        userRepository.save(user2);

        Department otherDept = departmentRepository.save(new Department("생산부"));

        // when
        List<User> salesUsers = userRepository.findByDepartmentDepartmentId(savedDepartment.getDepartmentId());
        List<User> prodUsers = userRepository.findByDepartmentDepartmentId(otherDept.getDepartmentId());

        // then
        assertThat(salesUsers).hasSize(2);
        assertThat(prodUsers).isEmpty();
    }

    @Test
    @DisplayName("상태로 사용자 목록을 조회할 수 있다")
    void findByUserStatus() {
        // given
        User retiredUser = User.builder()
                .employeeNo("EMP003")
                .userName("이영희")
                .userEmail("lee@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.RETIRED)
                .build();
        userRepository.save(retiredUser);

        // when
        List<User> activeUsers = userRepository.findByUserStatus(UserStatus.ACTIVE);
        List<User> retiredUsers = userRepository.findByUserStatus(UserStatus.RETIRED);

        // then
        assertThat(activeUsers).hasSize(1);
        assertThat(retiredUsers).hasSize(1);
    }

    @Test
    @DisplayName("사용자 저장 시 createdAt과 updatedAt이 자동 설정된다")
    void saveUser_setsCreatedAtAndUpdatedAt() {
        // given
        User newUser = User.builder()
                .employeeNo("EMP099")
                .userName("prePersist 테스트")
                .userEmail("prepersist@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // when
        User saved = userRepository.saveAndFlush(newUser);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 정보 수정 시 updatedAt이 갱신된다")
    void updateUser_updatesTimestamp() {
        // given
        savedUser.updateInfo("김길동", "kim@test.com");

        // when
        userRepository.saveAndFlush(savedUser);
        User updated = userRepository.findById(savedUser.getUserId()).orElseThrow();

        // then
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUserName()).isEqualTo("김길동");
    }
}
