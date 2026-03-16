package com.sprint.findex.domain.dashboard.service;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexRankingResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexSummaryResponse;

import java.util.List;

public interface DashboardService {
    //즐겨찾기 지수 성과 조회
    List<IndexSummaryResponse> getFavoritePerformance(PeriodType periodType);

    // 지수 차트 초회
    IndexChartResponse getChart(Long indexInfoId, ChartPeriodType periodType);

    // 성과 랭킹 조회
    List<IndexRankingResponse> getPerformanceRank(Long indexInfoId, PeriodType periodType);
}
