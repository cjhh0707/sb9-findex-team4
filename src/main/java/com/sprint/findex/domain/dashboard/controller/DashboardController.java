package com.sprint.findex.domain.dashboard.controller;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartDto;
import com.sprint.findex.domain.dashboard.dto.response.IndexPerformanceDto;
import com.sprint.findex.domain.dashboard.dto.response.RankedIndexPerformanceDto;
import com.sprint.findex.domain.dashboard.service.DashboardService;
import com.sprint.findex.domain.dashboard.service.basic.BasicDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final BasicDashboardService basicDashboardService;

    public DashboardController(BasicDashboardService basicDashboardService) {
        this.basicDashboardService = basicDashboardService;
    }

    /** 즐겨찾기로 등록된 지수들의 성과를 조회합니다. (1번째 부분 - 주요 지수) */
    @GetMapping("/index-data/performance/favorite")
    public List<IndexPerformanceDto> getFavPerformance(@RequestParam(value = "periodType", defaultValue = "DAILY") PeriodType periodType) {
        return basicDashboardService.getFavoritePerformance(periodType);
    }

    /** 지정된 지수 ID와 기간 유형에 해당하는 차트 데이터를 조회합니다. (2번째 부분 - 지수 차트) */
    @GetMapping("/index-data/{id}/chart")
    public IndexChartDto getChart(
            @PathVariable("id") long id, @RequestParam(value = "periodType", defaultValue = "MONTHLY") ChartPeriodType periodType) {
        return basicDashboardService.getChart(id, periodType);
    }

    /**
     * 특정 지수 정보를 기준으로 성과 순위를 조회합니다. 이 메소드는 주어진 기간과 제한된 수량에 따라 지수들의 성과 순위 목록을 반환합니다. (3번째 부분 - 지수 성과)
     */
    @GetMapping("/index-data/performance/rank")
    public List<RankedIndexPerformanceDto> getPerformanceRank(
            @RequestParam(name = "indexInfoId", required = false) Long indexInfoId,
            @RequestParam(value = "periodType", defaultValue = "DAILY") PeriodType periodType,
            @RequestParam(defaultValue = "10") int limit) {
        return basicDashboardService.getPerformanceRank(indexInfoId, periodType, limit);
    }
}
