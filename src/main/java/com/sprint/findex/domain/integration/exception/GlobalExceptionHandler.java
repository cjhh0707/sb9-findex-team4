package com.sprint.findex.domain.integration.exception;

import com.sprint.findex.domain.integration.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 400 Bad Request (잘못된 요청 파라미터 등이 들어왔을 때)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
    log.error("[400 Error] url: {}, message: {}", request.getRequestURI(), e.getMessage());

    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .message("잘못된 요청입니다.")
        .details(e.getMessage())
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 404 Not Found (요청한 데이터를 DB 등에서 찾을 수 없을 때)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e, HttpServletRequest request) {
    log.error("[404 Error] url: {}, message: {}", request.getRequestURI(), e.getMessage());

    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .message("데이터를 찾을 수 없습니다.")
        .details(e.getMessage())
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  // 500 Internal Server Error (서버 내부에서 예상치 못한 에러가 터졌을 때)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e, HttpServletRequest request) {
    log.error("[500 Error] url: {}, message: {}", request.getRequestURI(), e.getMessage(), e);

    ErrorResponse response = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message("서버 오류입니다.")
        .details(e.getMessage())
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
