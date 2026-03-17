package com.sprint.findex.domain.indexdata.dto;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,    // 실제 데이터 목록
    boolean hasNext,    // 다음 페이지 존재 여부
    Long nextCursor     // 다음 조회를 위한 커서 ID (idAfter로 사용될 값)
) {
  // 혹시 몰라 정적 팩토리 메서드도 넣어두었습니다.
  public static <T> CursorPageResponse<T> of(List<T> content, boolean hasNext, Long nextCursor) {
    return new CursorPageResponse<>(content, hasNext, nextCursor);
  }
}