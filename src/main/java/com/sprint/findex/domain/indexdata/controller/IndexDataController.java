package com.sprint.findex.domain.indexdata.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexdata.dto.*;
import com.sprint.findex.domain.indexdata.service.IndexDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 생성합니다.")
  @PostMapping
  public ResponseEntity<IndexDataResponse> save(@RequestBody IndexDataCreateRequest request) {
    return ResponseEntity.status(201).body(indexDataService.save(request));
  }

  @Operation(summary = "지수 데이터 목록 조회", description = "검색 조건에 따른 지수 데이터를 커서 기반 페이징으로 조회합니다.")
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataResponse>> search(
          @RequestParam(name = "indexInfoId", required = false) Long indexInfoId,
          @RequestParam(required = false) LocalDate startDate,
          @RequestParam(required = false) LocalDate endDate,
          @RequestParam(name = "idAfter", required = false) Long idAfter,
          @RequestParam(required = false) String cursor,
          @RequestParam(defaultValue = "baseDate") String sortField,
          @RequestParam(defaultValue = "desc") String sortDirection,
          @RequestParam(defaultValue = "1000") int size) {

    // [수정 부분] cursor와 idAfter 중 있는 값을 안전하게 선택
    Long finalIdAfter = idAfter;
    if (cursor != null && !cursor.isBlank()) {
      try {
        finalIdAfter = Long.parseLong(cursor);
      } catch (NumberFormatException e) {
        finalIdAfter = idAfter; // 숫자가 아니면 idAfter 값 유지
      }
    }

    CursorPageResponse<IndexDataResponse> response = indexDataService.search(
            indexInfoId, startDate, endDate, finalIdAfter, sortField, sortDirection, size);

    return ResponseEntity.ok(response);
  }
  @Operation(summary = "지수 데이터 수정", description = "특정 ID의 지수 데이터를 수정합니다.")
  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataResponse> update(
          @PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
    return ResponseEntity.ok(indexDataService.update(id, request));
  }

  @Operation(summary = "지수 데이터 삭제", description = "특정 ID의 지수 데이터를 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexDataService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "지수 데이터 CSV 내보내기", description = "검색된 지수 데이터를 CSV 파일 형식으로 다운로드합니다.")
  @GetMapping("/export/csv")
  public ResponseEntity<byte[]> exportCsv(
          @RequestParam(required = false) Long indexInfoId,
          @RequestParam(required = false) LocalDate startDate,
          @RequestParam(required = false) LocalDate endDate,
          @RequestParam(defaultValue = "baseDate") String sortField,
          @RequestParam(defaultValue = "desc") String sortDirection) {

    String csvData = indexDataService.exportToCsv(indexInfoId, startDate, endDate, sortField, sortDirection);
    String fileName = "index_data_" + LocalDate.now() + ".csv";

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .body(csvData.getBytes(StandardCharsets.UTF_8));
  }
}