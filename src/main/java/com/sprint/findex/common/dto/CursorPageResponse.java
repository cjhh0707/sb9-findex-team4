package com.sprint.findex.common.dto;

import java.util.List;

public record CursorPageResponse<T> (
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}