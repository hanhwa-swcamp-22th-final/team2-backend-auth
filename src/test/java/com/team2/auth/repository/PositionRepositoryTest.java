package com.team2.auth.repository;

import com.team2.auth.entity.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = MybatisAutoConfiguration.class)
class PositionRepositoryTest {

    @Autowired
    private PositionRepository positionRepository;

    @BeforeEach
    void setUp() {
        positionRepository.save(new Position("팀장", 1));
        positionRepository.save(new Position("팀원", 2));
    }

    @Test
    @DisplayName("직급명으로 직급을 조회할 수 있다")
    void findByPositionName() {
        // given
        String name = "팀장";

        // when
        Optional<Position> result = positionRepository.findByPositionName(name);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPositionLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("직급 저장 시 createdAt이 자동 설정된다")
    void savePosition_setsCreatedAt() {
        // given
        Position position = new Position("사원", 3);

        // when
        Position saved = positionRepository.saveAndFlush(position);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 직급명으로 조회하면 빈 Optional을 반환한다")
    void findByPositionName_notFound() {
        // given
        String name = "임원";

        // when
        Optional<Position> result = positionRepository.findByPositionName(name);

        // then
        assertThat(result).isEmpty();
    }
}
