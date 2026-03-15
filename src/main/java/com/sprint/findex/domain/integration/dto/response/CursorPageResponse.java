package com.sprint.findex.domain.integration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
  private List<T> content;        // 실제 데이터 목록 (IntegrationResponse 리스트)
  private String nextCursor;      // 다음 페이지 조회용 커서 (Base64 등, 없으면 null)
  private Long nextIdAfter;       // 다음 페이지 조회용 ID (없으면 null)
  private Integer size;           // 현재 페이지 데이터 개수
  private Long totalElements;     // 전체 데이터 개수 (옵션)
  private Boolean hasNext;        // 다음 페이지 존재 여부
}