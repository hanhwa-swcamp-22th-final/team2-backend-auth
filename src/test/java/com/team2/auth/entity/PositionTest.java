package com.team2.auth.entity;

import com.team2.auth.command.repository.PositionRepository;
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
class PositionTest {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("직급 생성 성공: 이름과 레벨이 정상 설정된다.")
    void createPosition_Success() {
        // given & when
        Position position = new Position("팀장", 1);

        // then - 도메인 로직 검증
        assertEquals("팀장", position.getPositionName());
        assertEquals(1, position.getPositionLevel());

        // DB 저장 후 재조회 검증
        positionRepository.save(position);
        entityManager.flush();
        entityManager.clear();

        Position found = positionRepository.findById(position.getPositionId()).orElseThrow();
        assertEquals("팀장", found.getPositionName());
        assertEquals(1, found.getPositionLevel());
        // @PrePersist로 createdAt 자동설정 확인
        assertNotNull(found.getCreatedAt());
    }

    @Test
    @DisplayName("직급 생성 실패: 이름이 null이면 예외가 발생한다.")
    void createPositionNameIsNullThrowsException() {
        // given & when & then
        assertThrows(IllegalArgumentException.class, () -> new Position(null, 1));
    }

    @Test
    @DisplayName("직급 생성 실패: 이름이 공백이면 예외가 발생한다.")
    void createPositionNameIsBlankThrowsException() {
        // given & when & then
        assertThrows(IllegalArgumentException.class, () -> new Position(" ", 1));
    }

    @Test
    @DisplayName("결재 권한 확인: level=1이면 결재 권한을 보유한다.")
    void hasApprovalAuthority_Level1_ReturnsTrue() {
        // given
        Position position = new Position("팀장", 1);

        // when & then
        assertTrue(position.hasApprovalAuthority());

        // DB 저장 후 재조회 검증
        positionRepository.save(position);
        entityManager.flush();
        entityManager.clear();

        Position found = positionRepository.findById(position.getPositionId()).orElseThrow();
        assertTrue(found.hasApprovalAuthority());
    }

    @Test
    @DisplayName("결재 권한 확인: level=2이면 결재 권한이 없다.")
    void hasApprovalAuthority_Level2_ReturnsFalse() {
        // given
        Position position = new Position("팀원", 2);

        // when & then
        assertFalse(position.hasApprovalAuthority());

        // DB 저장 후 재조회 검증
        positionRepository.save(position);
        entityManager.flush();
        entityManager.clear();

        Position found = positionRepository.findById(position.getPositionId()).orElseThrow();
        assertFalse(found.hasApprovalAuthority());
    }
}
