package com.sprint.findex.domain.integration.dto.response;

import io.swagger.v3.oas.annotations.media.Schema; // 👈 추가된 임포트
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

  @Schema(description = "에러 발생 시간", example = "2025-03-06T05:39:06.152068Z")
  private LocalDateTime timestamp;  // 에러 발생 시간

  @Schema(description = "상태 코드 (400, 500 등)", example = "400")
  private int status;               // 상태 코드

  @Schema(description = "클라이언트에게 보여줄 메시지", example = "잘못된 요청입니다.")
  private String message;           // 클라이언트에게 보여줄 메시지

  @Schema(description = "상세 에러 내용", example = "부서 코드는 필수입니다.")
  private String details;           // 상세 에러 내용
}