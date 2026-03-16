package com.sprint.findex.domain.indexdata.controller;

import com.sprint.findex.domain.indexdata.dto.CursorPageResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataListResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataSearchCondition;
import com.sprint.findex.domain.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.findex.domain.indexdata.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  /**
   * [POST] 지수 데이터 등록
   */
  @PostMapping
  public ResponseEntity<IndexDataResponse> save(@RequestBody IndexDataCreateRequest request) {
    IndexDataResponse response = indexDataService.save(request);
    return ResponseEntity.ok(response);
  }

  /**
   * [GET] 지수 데이터 목록 조회 및 검색
   * 팀장님 피드백: API 명세서에 맞게 idAfter 파라미터와 CursorPageResponse 반환 타입 적용
   */
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataListResponse>> search(
      IndexDataSearchCondition condition,
      @RequestParam(required = false) Long idAfter, // "lastId" 대신 명세서 이름인 "idAfter" 사용
      @RequestParam(defaultValue = "10") int size) { // 페이지 번호 대신 사이즈만 받음

    // idAfter와 size를 넘겨줌
    CursorPageResponse<IndexDataListResponse> response = indexDataService.search(condition, idAfter, size);

    return ResponseEntity.ok(response);
  }

  /**
   * [GET] 지수 데이터 단건 상세 조회
   */
  @GetMapping("/{id}")
  public ResponseEntity<IndexDataResponse> findById(@PathVariable Long id) {
    IndexDataResponse response = indexDataService.findById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * [PUT] 지수 데이터 수정
   * 기획서 사양: {지수}, {날짜}를 제외한 모든 속성을 수정할 수 있습니다. [cite: 31, 34]
   */
  @PutMapping("/{id}")
  public ResponseEntity<IndexDataResponse> update(
      @PathVariable Long id,
      @RequestBody IndexDataUpdateRequest request) {

    IndexDataResponse response = indexDataService.update(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * [DELETE] 지수 데이터 삭제
   * 기획서 사양: 지수 데이터를 시스템에서 삭제할 수 있습니다. [cite: 31, 34]
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexDataService.delete(id);
    return ResponseEntity.ok().build();
  }

  /**
   * [GET] 지수 데이터 CSV Export
   * 기획서 사양: 지수 데이터 목록 조회와 같은 규칙으로 필터링 및 정렬하여 CSV 파일로 추출 (페이징 제외)
   */
  @GetMapping("/export")
  public ResponseEntity<byte[]> exportCsv(IndexDataSearchCondition condition) {
    // 서비스에서 CSV 형식으로 변환된 문자열 데이터를 가져옵니다.
    String csvData = indexDataService.exportToCsv(condition);

    // 파일명을 "index_data_현재날짜.csv"로 설정합니다.
    String fileName = "index_data_" + java.time.LocalDate.now() + ".csv";

    // 브라우저가 파일로 인식하여 즉시 다운로드하도록 헤더 정보를 설정하여 응답합니다.
    return ResponseEntity.ok()
        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + fileName)
        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
        .body(csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}