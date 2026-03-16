//package com.sprint.findex.domain.autointegration.exception;
//
//import com.sprint.findex.domain.autointegration.dto.ErrorResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//  @ExceptionHandler(IllegalArgumentException.class)
//  @ResponseStatus(HttpStatus.BAD_REQUEST)
//  public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
//    return new ErrorResponse(
//        LocalDateTime.now(),
//        400,
//        "잘못된 요청입니다.",
//        ex.getMessage()
//    );
//  }
//
//  @ExceptionHandler(RuntimeException.class)
//  @ResponseStatus(HttpStatus.NOT_FOUND)
//  public ErrorResponse handleNotFound(RuntimeException ex) {
//    return new ErrorResponse(
//        LocalDateTime.now(),
//        404,
//        "리소스를 찾을 수 없습니다.",
//        ex.getMessage()
//    );
//  }
//
//  @ExceptionHandler(Exception.class)
//  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//  public ErrorResponse handleServerError(Exception ex) {
//    return new ErrorResponse(
//        LocalDateTime.now(),
//        500,
//        "서버 오류",
//        ex.getMessage()
//    );
//  }
//}
