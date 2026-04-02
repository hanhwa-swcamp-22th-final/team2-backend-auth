package com.team2.auth.common;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage
) {
    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResponse<>(content, totalElements, totalPages, page);
    }
}
