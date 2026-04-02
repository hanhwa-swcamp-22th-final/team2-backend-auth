package com.team2.auth.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseTest {

    @Test
    @DisplayName("of 메서드로 페이징 응답을 올바르게 생성한다")
    void of_success() {
        // given
        List<String> content = List.of("item1", "item2", "item3");

        // when
        PagedResponse<String> response = PagedResponse.of(content, 10, 0, 3);

        // then
        assertThat(response.content()).isEqualTo(content);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(4); // ceil(10/3) = 4
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("빈 리스트로 페이징 응답을 생성한다")
    void of_emptyList() {
        // given
        List<String> content = List.of();

        // when
        PagedResponse<String> response = PagedResponse.of(content, 0, 0, 10);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);
        assertThat(response.currentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("size가 0이면 totalPages는 0이다")
    void of_sizeZero() {
        // given
        List<String> content = List.of("item1");

        // when
        PagedResponse<String> response = PagedResponse.of(content, 5, 0, 0);

        // then
        assertThat(response.totalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("totalElements가 size로 나누어떨어지면 totalPages가 정확하다")
    void of_exactDivision() {
        // given
        List<String> content = List.of("a", "b");

        // when
        PagedResponse<String> response = PagedResponse.of(content, 10, 2, 5);

        // then
        assertThat(response.totalPages()).isEqualTo(2); // 10/5 = 2
        assertThat(response.currentPage()).isEqualTo(2);
    }
}
