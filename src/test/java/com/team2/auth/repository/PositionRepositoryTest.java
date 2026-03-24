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
        positionRepository.save(new Position("사원", 5));
        positionRepository.save(new Position("대리", 4));
    }

    @Test
    @DisplayName("직급명으로 직급을 조회할 수 있다")
    void findByName() {
        // given
        String name = "사원";

        // when
        Optional<Position> result = positionRepository.findByName(name);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLevel()).isEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 직급명으로 조회하면 빈 Optional을 반환한다")
    void findByName_notFound() {
        // given
        String name = "임원";

        // when
        Optional<Position> result = positionRepository.findByName(name);

        // then
        assertThat(result).isEmpty();
    }
}
