package com.sprint.findex.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexChartResponse {
    private LocalDate baseDate;
    private BigDecimal closingPrice;
    private BigDecimal movingAverage5;
    private BigDecimal movingAverager20;
}
