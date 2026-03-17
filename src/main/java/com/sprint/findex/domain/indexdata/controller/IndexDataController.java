package com.sprint.findex.domain.indexdata.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexdata.dto.*;
import com.sprint.findex.domain.indexdata.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  @PostMapping
  public ResponseEntity<IndexDataResponse> save(@RequestBody IndexDataCreateRequest request) {
    return ResponseEntity.status(201).body(indexDataService.save(request));
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataResponse>> search(
          @RequestParam(required = false) Long indexInfoId,
          @RequestParam(required = false) LocalDate startDate,
          @RequestParam(required = false) LocalDate endDate,
          @RequestParam(required = false) Long idAfter,
          @RequestParam(required = false) String cursor,
          @RequestParam(defaultValue = "baseDate") String sortField,
          @RequestParam(defaultValue = "desc") String sortDirection,
          @RequestParam(defaultValue = "10") int size) {

    Long finalIdAfter = (cursor != null && !cursor.isBlank()) ? Long.parseLong(cursor) : idAfter;

    CursorPageResponse<IndexDataResponse> response = indexDataService.search(
            indexInfoId, startDate, endDate, finalIdAfter, sortField, sortDirection, size);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<IndexDataResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(indexDataService.findById(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataResponse> update(
          @PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
    return ResponseEntity.ok(indexDataService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexDataService.delete(id);
    return ResponseEntity.noContent().build();
  }

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