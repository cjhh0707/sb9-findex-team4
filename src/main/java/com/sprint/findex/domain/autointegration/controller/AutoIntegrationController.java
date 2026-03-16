package com.sprint.findex.domain.autointegration.controller;

import com.sprint.findex.domain.autointegration.dto.AutoIntegrationCreateDto;
import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.dto.AutoIntegrationUpdateDto;
import com.sprint.findex.domain.autointegration.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// API 명세서 URI 루트 -> /api/auto-sync-configs/{id} 맞춰 진행
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoIntegrationController {

  private final AutoIntegrationService autoIntegrationService;

  // 새로운 자동 연동 설정 생성
  @PostMapping
  public AutoIntegrationDto create(@RequestBody AutoIntegrationCreateDto dto) {
    return autoIntegrationService.createAutoIntegration(dto.indexInfoId());
  }

  // ID로 조회
  @GetMapping("/{id}")
  public AutoIntegrationDto getById(@PathVariable Long id) {
    return autoIntegrationService.getAutoIntegration(id);
  }

  // Index ID로 조회
  @GetMapping("/by-index/{indexId}")
  public AutoIntegrationDto getByIndex(@PathVariable Long indexId) {
    return autoIntegrationService.getAutoIntegration(indexId);
  }

//  활성화 상태 기준 전체 조회
@GetMapping
public CursorPageResponse<AutoIntegrationDto> getAutoSyncConfigs(
    @RequestParam(required = false) Long indexInfoId,
    @RequestParam(required = false) Boolean enabled,
    @RequestParam(required = false) Long idAfter,
    @RequestParam(required = false) String cursor,
    @RequestParam(defaultValue = "indexName") String sortField,
    @RequestParam(defaultValue = "asc") String sortDirection,
    @RequestParam(defaultValue = "10") int size
) {

  return autoIntegrationService.getAutoSyncConfigs(
      indexInfoId,
      enabled,
      idAfter,
      cursor,
      sortField,
      sortDirection,
      size
  );
}

  // 활성화 상태 업데이트
  @PatchMapping("/{id}")
  public AutoIntegrationDto updateEnabled(
      @PathVariable Long id,
      @RequestBody AutoIntegrationUpdateDto dto) {
    return autoIntegrationService.updateEnabled(id, dto.enabled());
  }

  // 마지막 동기화 날짜 업데이트
  @PatchMapping("/{id}/last-sync")
  public AutoIntegrationDto updateLastSync(@PathVariable Long id) {
    return autoIntegrationService.updateLastIntegrationDate(id, null); // 날짜는 서비스에서 now 처리
  }
}