package com.sprint.findex.domain.integration.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @Operation(summary = "연동 작업 목록 조회", description = "연동 작업 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CursorPageResponse<IntegrationResponse>> getSyncJobs(
            @org.springdoc.core.annotations.ParameterObject @ModelAttribute IntegrationSearchCondition conditionDto) {

        CursorPageResponse<IntegrationResponse> response = integrationService.getIntegrations(conditionDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
    @PostMapping("/index-infos")
    public ResponseEntity<List<IntegrationResponse>> syncIndexInfos(HttpServletRequest httpRequest) {
        String workerIp = getClientIp(httpRequest);

        // 서비스에 작업 접수를 지시하고, 생성된 'NEW' 상태의 이력 리스트를 받습니다.
        List<IntegrationResponse> response = integrationService.createIndexInfoSyncJob(workerIp);
        return ResponseEntity.status(202).body(response);
    }

    @Operation(summary = "지수 데이터 연동 실행", description = "선택한 지수의 데이터를 공공데이터포털에서 가져와 비동기로 연동합니다.")
    @PostMapping("/index-data")
    public ResponseEntity<List<IntegrationResponse>> syncIndexData(
            @RequestBody IntegrationSyncRequest request,
            HttpServletRequest httpRequest) {

        String workerIp = getClientIp(httpRequest);

        // 작업(NEW)을 먼저 생성하여 반환하고 비동기 실행하도록 서비스 메서드 변경
        List<IntegrationResponse> response = integrationService.createIndexDataSyncJob(request, workerIp);

        return ResponseEntity.status(202).body(response);
    }

    // IP 추출 로직 (유지)
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}