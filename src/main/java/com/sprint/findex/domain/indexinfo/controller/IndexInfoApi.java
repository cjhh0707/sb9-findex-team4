package com.sprint.findex.domain.indexinfo.controller;


import com.sprint.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지수 정보 관리 API", description = "지수 정보의 등록, 수정, 삭제 및 조회를 담당합니다.")
public interface IndexInfoApi {

    @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 등록합니다. 지수 분류명과 지수명 조합은 중복될 수 없습니다.")
    @PostMapping
    ResponseEntity<IndexInfoResponse> createIndexInfo(@Valid @RequestBody IndexInfoCreateRequest request);

    @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다. (채용 종목 수, 기준 시점, 기준 지수, 즐겨찾기 변경 가능)")
    @PutMapping("/{id}")
    ResponseEntity<IndexInfoResponse> updateIndexInfo(@PathVariable("id") Long id, @Valid @RequestBody IndexInfoUpdateRequest request);

    @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteIndexInfo(@PathVariable("id") Long id);

    @Operation(summary = "지수 정보 목록 조회", description = "조건에 맞는 지수 정보 목록을 페이징하여 조회합니다.")
    @GetMapping
    ResponseEntity<List<IndexInfoResponse>> getIndexInfoList(@ModelAttribute IndexInfoSearchCondition condition);

}