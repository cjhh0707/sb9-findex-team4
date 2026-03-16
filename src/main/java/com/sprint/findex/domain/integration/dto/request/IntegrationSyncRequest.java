package com.sprint.findex.domain.integration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema; // 👈 추가된 임포트
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class IntegrationSyncRequest {

  @Schema(description = "연동할 지수 정보 ID 목록", example = "[1, 2, 3]")
  private List<Long> indexInfoIds;  // 연동할 지수 정보 ID 목록

  @Schema(description = "연동 시작 대상 날짜", example = "2023-01-01")
  private LocalDate baseDateFrom;   // 연동 시작 대상 날짜

  @Schema(description = "연동 종료 대상 날짜", example = "2023-01-31")
  private LocalDate baseDateTo;     // 연동 종료 대상 날짜
}