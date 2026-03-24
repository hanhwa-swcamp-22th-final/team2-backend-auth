package com.team2.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    @DisplayName("직급 생성 성공: 이름과 레벨이 정상 설정된다.")
    void createPosition_Success() {
        // given & when
        Position position = new Position("팀장", 1);

        // then
        assertEquals("팀장", position.getName());
        assertEquals(1, position.getLevel());
    }

    @Test
    @DisplayName("결재 권한 확인: level=1이면 결재 권한을 보유한다.")
    void hasApprovalAuthority_Level1_ReturnsTrue() {
        // given
        Position position = new Position("팀장", 1);

        // when & then
        assertTrue(position.hasApprovalAuthority());
    }

    @Test
    @DisplayName("결재 권한 확인: level=2이면 결재 권한이 없다.")
    void hasApprovalAuthority_Level2_ReturnsFalse() {
        // given
        Position position = new Position("팀원", 2);

        // when & then
        assertFalse(position.hasApprovalAuthority());
    }
}
