package com.team2.auth.service;

import com.team2.auth.entity.Position;
import com.team2.auth.repository.PositionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class PositionServiceTest {

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("직급을 생성할 수 있다")
    void createPosition_success() {
        // when
        Position result = positionService.createPosition("팀원", 2);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("팀원");
        assertThat(result.getLevel()).isEqualTo(2);

        // DB에 실제 저장 확인
        entityManager.flush();
        entityManager.clear();
        assertThat(positionRepository.findByName("팀원")).isPresent();
    }

    @Test
    @DisplayName("전체 직급 목록을 조회할 수 있다")
    void getAllPositions() {
        // given
        positionService.createPosition("팀장", 1);
        positionService.createPosition("팀원", 2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Position> result = positionService.getAllPositions();

        // then
        assertThat(result).hasSize(2);
    }
}
