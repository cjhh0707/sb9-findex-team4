package com.sprint.findex.domain.autointegration.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.dto.AutoIntegrationUpdateDto;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자동 연동 설정 API", description = "자동 연동 설정 관리 API")
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoIntegrationController {

    private final AutoIntegrationService autoIntegrationService;

    @Operation(summary = "자동 연동 설정 목록 조회", description = "자동 연동 설정 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CursorPageResponse<AutoIntegrationDto>> getAutoSyncConfigs(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "indexInfo.indexName") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "200") int size
    ) {
        return ResponseEntity.ok(autoIntegrationService.getAutoSyncConfigs(
                indexInfoId, enabled, idAfter, cursor, sortField, sortDirection, size));
    }

    @Operation(summary = "자동 연동 설정 수정", description = "기존 자동 연동 설정을 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<AutoIntegrationDto> updateEnabled(
            @PathVariable Long id,
            @RequestBody AutoIntegrationUpdateDto dto) {
        return ResponseEntity.ok(autoIntegrationService.updateEnabled(id, dto.enabled()));
    }
}
