package com.team2.auth.entity;

import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User createDefaultUser() {
        return User.builder()
                .employeeNo("EMP001")
                .name("홍길동")
                .email("hong@test.com")
                .pw("hashedPassword")
                .role(Role.SALES)
                .status(UserStatus.재직)
                .build();
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("사용자 생성 성공: 필수 필드가 정상 설정된다.")
    void createUser_Success() {
        // given & when
        User user = createDefaultUser();

        // then
        assertEquals("EMP001", user.getEmployeeNo());
        assertEquals("홍길동", user.getName());
        assertEquals("hong@test.com", user.getEmail());
        assertEquals(Role.SALES, user.getRole());
        assertEquals(UserStatus.재직, user.getStatus());
    }

    // === 로그인 가능 여부 ===

    @Test
    @DisplayName("로그인 가능: 재직 상태이면 로그인 가능하다.")
    void canLogin_ActiveUser_ReturnsTrue() {
        // given
        User user = createDefaultUser();

        // when & then
        assertTrue(user.canLogin());
    }

    @Test
    @DisplayName("로그인 불가: 휴직 상태이면 로그인 불가하다.")
    void canLogin_OnLeaveUser_ReturnsFalse() {
        // given
        User user = User.builder()
                .employeeNo("EMP002")
                .name("김철수")
                .email("kim@test.com")
                .pw("hashedPassword")
                .role(Role.SALES)
                .status(UserStatus.휴직)
                .build();

        // when & then
        assertFalse(user.canLogin());
    }

    @Test
    @DisplayName("로그인 불가: 퇴직 상태이면 로그인 불가하다.")
    void canLogin_RetiredUser_ReturnsFalse() {
        // given
        User user = User.builder()
                .employeeNo("EMP003")
                .name("이영희")
                .email("lee@test.com")
                .pw("hashedPassword")
                .role(Role.SALES)
                .status(UserStatus.퇴직)
                .build();

        // when & then
        assertFalse(user.canLogin());
    }

    // === 상태 변경 ===

    @Test
    @DisplayName("상태 변경 성공: 재직에서 휴직으로 변경된다.")
    void changeStatus_ToOnLeave_Success() {
        // given
        User user = createDefaultUser();

        // when
        user.changeStatus(UserStatus.휴직);

        // then
        assertEquals(UserStatus.휴직, user.getStatus());
    }

    @Test
    @DisplayName("상태 변경 실패: 퇴직 상태에서는 상태를 변경할 수 없다.")
    void changeStatus_FromRetired_ThrowsException() {
        // given
        User user = User.builder()
                .employeeNo("EMP004")
                .name("박민수")
                .email("park@test.com")
                .pw("hashedPassword")
                .role(Role.SALES)
                .status(UserStatus.퇴직)
                .build();

        // when & then
        assertThrows(IllegalStateException.class,
                () -> user.changeStatus(UserStatus.재직));
    }

    // === 부서/직급 배정 ===

    @Test
    @DisplayName("부서 배정 성공: 사용자에게 부서를 배정할 수 있다.")
    void assignDepartment_Success() {
        // given
        User user = createDefaultUser();
        Department department = new Department("영업1팀");

        // when
        user.assignDepartment(department);

        // then
        assertEquals(department, user.getDepartment());
    }

    @Test
    @DisplayName("직급 배정 성공: 사용자에게 직급을 배정할 수 있다.")
    void assignPosition_Success() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀장", 1);

        // when
        user.assignPosition(position);

        // then
        assertEquals(position, user.getPosition());
    }

    // === 결재 권한 ===

    @Test
    @DisplayName("결재 권한 확인: 팀장 직급이면 결재 권한이 있다.")
    void hasApprovalAuthority_TeamLead_ReturnsTrue() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀장", 1);
        user.assignPosition(position);

        // when & then
        assertTrue(user.hasApprovalAuthority());
    }

    @Test
    @DisplayName("결재 권한 확인: 직급이 없으면 결재 권한이 없다.")
    void hasApprovalAuthority_NoPosition_ReturnsFalse() {
        // given
        User user = createDefaultUser();

        // when & then
        assertFalse(user.hasApprovalAuthority());
    }

    // === 관리자 확인 ===

    @Test
    @DisplayName("관리자 확인: role이 ADMIN이면 관리자이다.")
    void isAdmin_AdminRole_ReturnsTrue() {
        // given
        User user = User.builder()
                .employeeNo("EMP005")
                .name("관리자")
                .email("admin@test.com")
                .pw("hashedPassword")
                .role(Role.ADMIN)
                .status(UserStatus.재직)
                .build();

        // when & then
        assertTrue(user.isAdmin());
    }

    @Test
    @DisplayName("관리자 확인: role이 SALES이면 관리자가 아니다.")
    void isAdmin_SalesRole_ReturnsFalse() {
        // given
        User user = createDefaultUser();

        // when & then
        assertFalse(user.isAdmin());
    }

    // === 정보 수정 ===

    @Test
    @DisplayName("정보 수정: name이 null이면 기존 이름을 유지한다.")
    void updateInfo_withNullName_keepsOriginalName() {
        // given
        User user = createDefaultUser();

        // when
        user.updateInfo(null, "new@email.com");

        // then
        assertEquals("홍길동", user.getName());
        assertEquals("new@email.com", user.getEmail());
    }

    @Test
    @DisplayName("정보 수정: email이 null이면 기존 이메일을 유지한다.")
    void updateInfo_withNullEmail_keepsOriginalEmail() {
        // given
        User user = createDefaultUser();

        // when
        user.updateInfo("newName", null);

        // then
        assertEquals("newName", user.getName());
        assertEquals("hong@test.com", user.getEmail());
    }

    @Test
    @DisplayName("정보 수정: 두 값 모두 전달하면 모두 변경된다.")
    void updateInfo_withBothValues_updatesBoth() {
        // given
        User user = createDefaultUser();

        // when
        user.updateInfo("newName", "new@email.com");

        // then
        assertEquals("newName", user.getName());
        assertEquals("new@email.com", user.getEmail());
    }

    // === 결재 권한 (추가) ===

    @Test
    @DisplayName("결재 권한 확인: level이 1이 아닌 직급이면 결재 권한이 없다.")
    void hasApprovalAuthority_withNonLevel1Position_returnsFalse() {
        // given
        User user = createDefaultUser();
        Position position = new Position("팀원", 2);
        user.assignPosition(position);

        // when & then
        assertFalse(user.hasApprovalAuthority());
    }

    // === 상태 변경 (추가) ===

    @Test
    @DisplayName("상태 변경 성공: 재직에서 퇴직으로 변경된다.")
    void changeStatus_fromActiveToRetired_success() {
        // given
        User user = createDefaultUser();

        // when
        user.changeStatus(UserStatus.퇴직);

        // then
        assertEquals(UserStatus.퇴직, user.getStatus());
    }
}
