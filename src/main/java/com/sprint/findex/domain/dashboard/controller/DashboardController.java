package com.sprint.findex.domain.dashboard.controller;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexRankingResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexSummaryResponse;
import com.sprint.findex.domain.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "대시보드 API", description = "대시보드 관련 API")
public class DashboardController {
    private final DashboardService dashboardService;

    //즐겨찾기 지수 성과 조회
    @GetMapping("/index-data/performance/favorite")
    @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기로 등록된 지수들의 성과 조회")
    public ResponseEntity<List<IndexSummaryResponse>> getFavoritePerformance(
            @RequestParam PeriodType periodType
            ) {
        return ResponseEntity.ok(dashboardService.getFavoritePerformance(periodType));
    }

    //지수 차트 조회
    @GetMapping("/index-data/{id}/chart")
    @Operation(summary = "지수 차트 조회", description = "월/분기/년 단위 시계열 데이터와 이동평균선을 조회")
    public ResponseEntity<IndexChartResponse> getChart(
            @PathVariable Long id,
            @RequestParam ChartPeriodType periodType
            ) {
        return ResponseEntity.ok(dashboardService.getChart(id, periodType));
    }

    // 성과 랭킹 조회
    @GetMapping("/index-data/performance/rank")
    @Operation(summary = "지수 성과 랭킹 조회", description = "전일/전주/전월 대비 성과 랭킹을 조회합니다.")
    public ResponseEntity<List<IndexRankingResponse>> getPerformanceRank(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam PeriodType periodType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getPerformanceRank(indexInfoId, periodType));
    }
}
