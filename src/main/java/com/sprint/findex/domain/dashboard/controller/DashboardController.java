package com.sprint.findex.domain.dashboard.controller;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartDto;
import com.sprint.findex.domain.dashboard.dto.response.IndexPerformanceDto;
import com.sprint.findex.domain.dashboard.dto.response.RankedIndexPerformanceDto;
import com.sprint.findex.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/index-data") // ⭐ 경로 수정
@RequiredArgsConstructor
@Tag(name = "대시보드 API", description = "대시보드 관련 API")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/performance/favorite")
    @Operation(summary = "관심 지수 성과 조회")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoritePerformance(@RequestParam PeriodType periodType) {
        return ResponseEntity.ok(dashboardService.getFavoritePerformance(periodType));
    }

    @GetMapping("/{id}/chart")
    @Operation(summary = "지수 차트 조회")
    public ResponseEntity<IndexChartDto> getChart(@PathVariable("id") Long id, @RequestParam ChartPeriodType periodType) {
        return ResponseEntity.ok(dashboardService.getChart(id, periodType));
    }

    @GetMapping("/performance/rank")
    @Operation(summary = "지수 성과 랭킹 조회")
    public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam PeriodType periodType,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getPerformanceRank(indexInfoId, periodType, limit));
    }
}