package com.sprint.findex.domain.dashboard.service;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartDto;
import com.sprint.findex.domain.dashboard.dto.response.IndexPerformanceDto;
import com.sprint.findex.domain.dashboard.dto.response.RankedIndexPerformanceDto;

import java.util.List;

public interface DashboardService {
    // 즐겨찾기 지수 성과 조회
    List<IndexPerformanceDto> getFavoritePerformance(PeriodType periodType);

    // 지수 차트 조회 (반환 타입 수정)
    IndexChartDto getChart(Long indexInfoId, ChartPeriodType periodType);

    // 성과 랭킹 조회 (반환 타입 수정 및 limit 파라미터 추가)
    List<RankedIndexPerformanceDto> getPerformanceRank(Long indexInfoId, PeriodType periodType, int limit);
}