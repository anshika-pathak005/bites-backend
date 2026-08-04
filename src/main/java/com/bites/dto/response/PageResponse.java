package com.bites.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mirrors the frontend's PaginatedResult<T> type exactly — keep the two in sync.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
