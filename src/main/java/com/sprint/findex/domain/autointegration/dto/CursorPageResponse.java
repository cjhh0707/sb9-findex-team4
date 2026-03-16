package com.sprint.findex.domain.autointegration.dto;

import java.util.List;

// 컴파일 에러 해결
public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    int size,
    boolean hasNext

) {}
