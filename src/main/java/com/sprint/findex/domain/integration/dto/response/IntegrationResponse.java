package com.sprint.findex.domain.integration.dto.response;

import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class IntegrationResponse {
  private Long id;                // 이력 ID
  private Long indexInfoId;       // 지수 정보 ID
  private JobType jobType;        // 연동 유형
  private LocalDate targetDate;   // 대상 날짜
  private String worker;          // 작업자
  private LocalDateTime jobTime;  // 작업 일시
  private JobResult result;       // 연동 결과 (Swagger상 status)
}