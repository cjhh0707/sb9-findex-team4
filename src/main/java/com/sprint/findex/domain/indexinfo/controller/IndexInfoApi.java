package com.sprint.findex.domain.indexinfo.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexinfo.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
public interface IndexInfoApi {

    @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 등록합니다.")
    @PostMapping
    ResponseEntity<IndexInfoResponse> createIndexInfo(@Valid @RequestBody IndexInfoCreateRequest request);

    @Operation(summary = "지수 정보 조회", description = "ID로 지수 정보를 조회합니다.")
    @GetMapping("/{id}")
    ResponseEntity<IndexInfoResponse> getIndexInfo(@PathVariable("id") Long id);

    @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
    @PatchMapping("/{id}")
    ResponseEntity<IndexInfoResponse> updateIndexInfo(@PathVariable("id") Long id, @RequestBody IndexInfoUpdateRequest request);

    @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteIndexInfo(@PathVariable("id") Long id);

    @Operation(summary = "지수 정보 목록 조회", description = "지수 정보 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
    @GetMapping
    ResponseEntity<CursorPageResponse<IndexInfoResponse>> getIndexInfoList(
            @ModelAttribute IndexInfoSearchCondition condition,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "지수 정보 요약 목록 조회", description = "지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.")
    @GetMapping("/summaries")
    ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries();
}
