package com.sprint.findex.domain.integration.dto.request;

import com.sprint.findex.domain.integration.entity.JobType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class IntegrationSearchCondition {

  @Parameter(description = "연동 작업 유형 (INDEX_INFO, INDEX_DATA)", schema = @Schema(allowableValues = {"INDEX_INFO", "INDEX_DATA"}))
  private JobType jobType; // Enum을 사용하면 Swagger가 알아서 드롭다운으로 만들어줍니다.

  @Parameter(description = "지수 정보 ID")
  private Long indexInfoId;

  @Parameter(description = "대상 날짜 (부터)")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateFrom;

  @Parameter(description = "대상 날짜 (까지)")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateTo;

  @Parameter(description = "작업자")
  private String worker;

  @Parameter(description = "작업 일시 (부터)")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeFrom;

  @Parameter(description = "작업 일시 (까지)")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeTo;

  @Parameter(description = "작업 상태 (SUCCESS, FAILED)", schema = @Schema(allowableValues = {"SUCCESS", "FAILED"}))
  private String status;

  @Parameter(description = "이전 페이지 마지막 요소 ID")
  private Long idAfter;

  @Parameter(description = "커서 (다음 페이지 시작점)")
  private String cursor;

  @Parameter(description = "정렬 필드 (targetDate, jobTime)", schema = @Schema(allowableValues = {"targetDate", "jobTime"}))
  private String sortField;

  @Parameter(description = "정렬 방향 (asc, desc)", schema = @Schema(allowableValues = {"asc", "desc"}))
  private String sortDirection;

  @Parameter(description = "페이지 크기")
  private Integer size = 10; // 기본값 10
}