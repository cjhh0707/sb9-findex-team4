package com.sprint.findex.domain.indexdata.controller;

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
   * 기획서 사양: 지수(완전 일치), 날짜(범위), No-offset 페이징(lastId 활용)
   */
  @GetMapping
  public ResponseEntity<Slice<IndexDataListResponse>> search(
      IndexDataSearchCondition condition,
      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

    Slice<IndexDataListResponse> responses = indexDataService.search(condition, pageable);
    return ResponseEntity.ok(responses);
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
}