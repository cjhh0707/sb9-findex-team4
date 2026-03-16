package com.sprint.findex.domain.integration.controller;

import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
import com.sprint.findex.domain.integration.dto.response.CursorPageResponse;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "연동 이력 (Integration)", description = "공공데이터 연동 및 이력 조회 API")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @Operation(
            summary = "연동 작업 목록 조회",
            description = "연동 작업 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<CursorPageResponse<IntegrationResponse>> getSyncJobs(
            @org.springdoc.core.annotations.ParameterObject @ModelAttribute IntegrationSearchCondition conditionDto) {

        // 서비스 호출
        CursorPageResponse<IntegrationResponse> response = integrationService.getIntegrations(conditionDto);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "연동 작업 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/index-infos")
    public ResponseEntity<List<IntegrationResponse>> syncIndexInfos(HttpServletRequest httpRequest) {

        String workerIp = getClientIp(httpRequest);

        // 서비스에 작업 접수를 지시하고, 생성된 'NEW' 상태의 이력 리스트를 받습니다.
        List<IntegrationResponse> response = integrationService.createIndexInfoSyncJob(workerIp);

        // 202 Accepted 상태 코드와 함께 데이터를 반환합니다.
        return ResponseEntity.status(202).body(response);
    }


    @Operation(summary = "지수 데이터 연동 실행", description = "선택한 지수의 데이터를 공공데이터포털에서 가져와 비동기로 연동합니다.")
    @PostMapping("/index-data")
    public ResponseEntity<Void> syncIndexData(
            @RequestBody IntegrationSyncRequest request,
            HttpServletRequest httpRequest) {

        String workerIp = getClientIp(httpRequest);

        integrationService.syncIndexData(request, workerIp);

        return ResponseEntity.accepted().build();
    }

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