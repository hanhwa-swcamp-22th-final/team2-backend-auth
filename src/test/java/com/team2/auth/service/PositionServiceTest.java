package com.team2.auth.service;

import com.team2.auth.entity.Position;
import com.team2.auth.repository.PositionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private PositionService positionService;

    @Test
    @DisplayName("직급을 생성할 수 있다")
    void createPosition_success() {
        // given
        Position position = new Position("팀원", 2);
        given(positionRepository.save(any(Position.class))).willReturn(position);

        // when
        Position result = positionService.createPosition("팀원", 2);

        // then
        assertThat(result.getName()).isEqualTo("팀원");
        assertThat(result.getLevel()).isEqualTo(2);
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    @DisplayName("전체 직급 목록을 조회할 수 있다")
    void getAllPositions() {
        // given
        given(positionRepository.findAll()).willReturn(List.of(
                new Position("팀원", 2),
                new Position("팀장", 1)
        ));

        // when
        List<Position> result = positionService.getAllPositions();

        // then
        assertThat(result).hasSize(2);
    }
}
