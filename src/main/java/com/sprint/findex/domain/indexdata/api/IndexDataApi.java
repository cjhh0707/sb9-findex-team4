package com.sprint.findex.domain.indexdata.api;

import com.sprint.findex.domain.indexdata.dto.CursorPageResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataListResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataSearchCondition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RequestMapping("/api/index-data")
public interface IndexDataApi {

  @Operation(summary = "지수 데이터 목록 조회", description = "커서 기반 페이징이 적용된 목록 조회")
  @GetMapping
  ResponseEntity<CursorPageResponse<IndexDataListResponse>> search(
      IndexDataSearchCondition condition,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(defaultValue = "10") int size
  );

  @Operation(summary = "지수 데이터 CSV export")
  @GetMapping("/export/csv")
  ResponseEntity<byte[]> exportCsv();

  // 나머지 스웨거에 있는 기능들도 나중에 구현할 수 있게 미리 정의만 해둘 수 있습니다.
    /*
    @PostMapping
    ResponseEntity<Void> createIndexData(...);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteIndexData(@PathVariable Long id);
    */
}