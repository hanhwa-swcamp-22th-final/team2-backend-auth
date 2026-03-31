package com.team2.auth.service;

import com.team2.auth.command.service.PositionCommandService;
import com.team2.auth.query.service.PositionQueryService;
import com.team2.auth.entity.Position;
import com.team2.auth.command.repository.PositionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class PositionServiceTest {

    @Autowired
    private PositionCommandService positionCommandService;

    @Autowired
    private PositionQueryService positionQueryService;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("직급을 생성할 수 있다")
    void createPosition_success() {
        // when
        Position result = positionCommandService.createPosition("팀원", 2);

        // then
        assertThat(result.getPositionId()).isNotNull();
        assertThat(result.getPositionName()).isEqualTo("팀원");
        assertThat(result.getPositionLevel()).isEqualTo(2);

        // DB에 실제 저장 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(positionRepository.findByPositionName("팀원")).isPresent();
    }

    @Test
    @DisplayName("전체 직급 목록을 조회할 수 있다")
    void getAllPositions() {
        // given
        positionCommandService.createPosition("팀장", 1);
        positionCommandService.createPosition("팀원", 2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Position> result = positionRepository.findAll();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("ID로 직급을 조회할 수 있다")
    void getPosition_success() {
        Position saved = positionCommandService.createPosition("팀장", 1);
        entityManager.flush();
        entityManager.clear();

        Position result = positionQueryService.getPosition(saved.getPositionId());

        assertThat(result.getPositionName()).isEqualTo("팀장");
        assertThat(result.getPositionLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 직급 조회 시 예외가 발생한다")
    void getPosition_notFound() {
        assertThatThrownBy(() -> positionQueryService.getPosition(99999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이름으로 직급을 조회할 수 있다")
    void getPositionByName_success() {
        positionCommandService.createPosition("팀장", 1);
        entityManager.flush();
        entityManager.clear();

        Position result = positionQueryService.getPositionByName("팀장");

        assertThat(result.getPositionName()).isEqualTo("팀장");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 직급 조회 시 예외가 발생한다")
    void getPositionByName_notFound() {
        assertThatThrownBy(() -> positionQueryService.getPositionByName("없는직급"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("직급을 찾을 수 없습니다");
    }
}
