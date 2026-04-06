package com.team2.auth.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "페이지네이션 응답")
public record PagedResponse<T>(
        @Schema(description = "데이터 목록") List<T> content,
        @Schema(description = "전체 항목 수", example = "100") long totalElements,
        @Schema(description = "전체 페이지 수", example = "10") int totalPages,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int currentPage
) {
    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResponse<>(content, totalElements, totalPages, page);
    }
}
