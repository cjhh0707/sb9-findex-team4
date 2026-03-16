package com.sprint.findex.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexSummaryResponse {
    private Long id;
    private String indexClassificationName;
    private String indexName;
    private BigDecimal closingPrice;
    private BigDecimal versus;
    private BigDecimal fluctuationRate;
}
