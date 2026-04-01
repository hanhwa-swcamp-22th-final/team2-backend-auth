package com.team2.auth.command.domain.entity;

import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.command.domain.repository.DepartmentRepository;
import com.team2.auth.command.domain.repository.PositionRepository;
import com.team2.auth.command.domain.repository.UserRepository;
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
class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    private User createDefaultUser() {
        return User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("hashedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    private User saveAndReload(User user) {
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        return userRepository.findById(user.getUserId()).orElseThrow();
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사용자 생성 성공: 필수 필드가 정상 설정된다.")
    void createUser_Success() {
        // given & when
        User user = createDefaultUser();

        // then - 도메인 로직 검증
        assertEquals("EMP001", user.getEmployeeNo());
        assertEquals("홍길동", user.getUserName());
        assertEquals("hong@test.com", user.getUserEmail());
        assertEquals(Role.SALES, user.getUserRole());
        assertEquals(UserStatus.ACTIVE, user.getUserStatus());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertEquals("EMP001", found.getEmployeeNo());
        assertEquals("홍길동", found.getUserName());
        assertEquals("hong@test.com", found.getUserEmail());
        assertEquals(Role.SALES, found.getUserRole());
        assertEquals(UserStatus.ACTIVE, found.getUserStatus());
        // @PrePersist 검증
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    // === 로그인 가능 여부 ===

    @Test
    @DisplayName("로그인 가능: 재직 상태이면 로그인 가능하다.")
    void canLogin_ActiveUser_ReturnsTrue() {
        // given
        User user = createDefaultUser();

        // when & then
        assertTrue(user.canLogin());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertTrue(found.canLogin());
    }

    @Test
    @DisplayName("로그인 불가: 휴직 상태이면 로그인 불가하다.")
    void canLogin_OnLeaveUser_ReturnsFalse() {
        // given
        User user = User.builder()
                .employeeNo("EMP002")
                .userName("김철수")
                .userEmail("kim@test.com")
                .userPw("hashedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ON_LEAVE)
                .build();

        // when & then
        assertFalse(user.canLogin());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertFalse(found.canLogin());
    }

    @Test
    @DisplayName("로그인 불가: 퇴직 상태이면 로그인 불가하다.")
    void canLogin_RetiredUser_ReturnsFalse() {
        // given
        User user = User.builder()
                .employeeNo("EMP003")
                .userName("이영희")
                .userEmail("lee@test.com")
                .userPw("hashedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.RETIRED)
                .build();

        // when & then
        assertFalse(user.canLogin());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertFalse(found.canLogin());
    }

    // === 상태 변경 ===

    @Test
    @DisplayName("상태 변경 성공: 재직에서 휴직으로 변경된다.")
    void changeStatus_ToOnLeave_Success() {
        // given
        User user = createDefaultUser();

        // when
        user.changeStatus(UserStatus.ON_LEAVE);

        // then
        assertEquals(UserStatus.ON_LEAVE, user.getUserStatus());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertEquals(UserStatus.ON_LEAVE, found.getUserStatus());
    }

    @Test
    @DisplayName("상태 변경 실패: 퇴직 상태에서는 상태를 변경할 수 없다.")
    void changeStatus_FromRetired_ThrowsException() {
        // given
        User user = User.builder()
                .employeeNo("EMP004")
                .userName("박민수")
                .userEmail("park@test.com")
                .userPw("hashedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.RETIRED)
                .build();

        // when & then
        assertThrows(IllegalStateException.class,
                () -> user.changeStatus(UserStatus.ACTIVE));
    }

    // === 부서/직급 배정 ===

    @Test
    @DisplayName("부서 배정 성공: 사용자에게 부서를 배정할 수 있다.")
    void assignDepartment_Success() {
        // given
        User user = createDefaultUser();
        Department department = new Department("영업1팀");
        departmentRepository.save(department);

        // when
        user.assignDepartment(department);

        // then
        assertEquals(department, user.getDepartment());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertEquals("영업1팀", found.getDepartment().getDepartmentName());
    }

    @Test
    @DisplayName("직급 배정 성공: 사용자에게 직급을 배정할 수 있다.")
    void assignPosition_Success() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀장", 1);
        positionRepository.save(position);

        // when
        user.assignPosition(position);

        // then
        assertEquals(position, user.getPosition());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertEquals("팀장", found.getPosition().getPositionName());
    }

    // === 결재 권한 ===

    @Test
    @DisplayName("결재 권한 확인: 팀장 직급이면 결재 권한이 있다.")
    void hasApprovalAuthority_TeamLead_ReturnsTrue() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀장", 1);
        positionRepository.save(position);
        user.assignPosition(position);

        // when & then
        assertTrue(user.hasApprovalAuthority());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertTrue(found.hasApprovalAuthority());
    }

    @Test
    @DisplayName("결재 권한 확인: 직급이 없으면 결재 권한이 없다.")
    void hasApprovalAuthority_NoPosition_ReturnsFalse() {
        // given
        User user = createDefaultUser();

        // when & then
        assertFalse(user.hasApprovalAuthority());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertFalse(found.hasApprovalAuthority());
    }

    // === 관리자 확인 ===

    @Test
    @DisplayName("관리자 확인: role이 ADMIN이면 관리자이다.")
    void isAdmin_AdminRole_ReturnsTrue() {
        // given
        User user = User.builder()
                .employeeNo("EMP005")
                .userName("관리자")
                .userEmail("admin@test.com")
                .userPw("hashedPassword")
                .userRole(Role.ADMIN)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // when & then
        assertTrue(user.isAdmin());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertTrue(found.isAdmin());
    }

    @Test
    @DisplayName("관리자 확인: role이 SALES이면 관리자가 아니다.")
    void isAdmin_SalesRole_ReturnsFalse() {
        // given
        User user = createDefaultUser();

        // when & then
        assertFalse(user.isAdmin());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertFalse(found.isAdmin());
    }

    // === 정보 수정 ===

    @Test
    @DisplayName("정보 수정: name이 null이면 기존 이름을 유지한다.")
    void updateInfo_withNullName_keepsOriginalName() {
        // given
        User user = createDefaultUser();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // when
        User found = userRepository.findById(user.getUserId()).orElseThrow();
        found.updateInfo(null, "new@email.com");

        // then
        assertEquals("홍길동", found.getUserName());
        assertEquals("new@email.com", found.getUserEmail());

        // DB 반영 후 재조회
        entityManager.flush();
        entityManager.clear();
        User reloaded = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("홍길동", reloaded.getUserName());
        assertEquals("new@email.com", reloaded.getUserEmail());
    }

    @Test
    @DisplayName("정보 수정: email이 null이면 기존 이메일을 유지한다.")
    void updateInfo_withNullEmail_keepsOriginalEmail() {
        // given
        User user = createDefaultUser();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // when
        User found = userRepository.findById(user.getUserId()).orElseThrow();
        found.updateInfo("newName", null);

        // then
        assertEquals("newName", found.getUserName());
        assertEquals("hong@test.com", found.getUserEmail());

        // DB 반영 후 재조회
        entityManager.flush();
        entityManager.clear();
        User reloaded = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("newName", reloaded.getUserName());
        assertEquals("hong@test.com", reloaded.getUserEmail());
    }

    @Test
    @DisplayName("정보 수정: 두 값 모두 전달하면 모두 변경된다.")
    void updateInfo_withBothValues_updatesBoth() {
        // given
        User user = createDefaultUser();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // when
        User found = userRepository.findById(user.getUserId()).orElseThrow();
        found.updateInfo("newName", "new@email.com");

        // then
        assertEquals("newName", found.getUserName());
        assertEquals("new@email.com", found.getUserEmail());

        // DB 반영 후 재조회
        entityManager.flush();
        entityManager.clear();
        User reloaded = userRepository.findById(user.getUserId()).orElseThrow();
        assertEquals("newName", reloaded.getUserName());
        assertEquals("new@email.com", reloaded.getUserEmail());
    }

    // === 결재 권한 (추가) ===

    @Test
    @DisplayName("결재 권한 확인: level이 1이 아닌 직급이면 결재 권한이 없다.")
    void hasApprovalAuthority_withNonLevel1Position_returnsFalse() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀원", 2);
        positionRepository.save(position);
        user.assignPosition(position);

        // when & then
        assertFalse(user.hasApprovalAuthority());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertFalse(found.hasApprovalAuthority());
    }

    // === 상태 변경 (추가) ===

    @Test
    @DisplayName("상태 변경 성공: 재직에서 퇴직으로 변경된다.")
    void changeStatus_fromActiveToRetired_success() {
        // given
        User user = createDefaultUser();

        // when
        user.changeStatus(UserStatus.RETIRED);

        // then
        assertEquals(UserStatus.RETIRED, user.getUserStatus());

        // DB 저장 후 재조회 검증
        User found = saveAndReload(user);
        assertEquals(UserStatus.RETIRED, found.getUserStatus());
    }
}
