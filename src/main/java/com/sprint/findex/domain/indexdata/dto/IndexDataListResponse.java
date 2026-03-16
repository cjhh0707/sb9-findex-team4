package com.sprint.findex.domain.indexdata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

/**
 * 지수 데이터 목록 조회용 응답 DTO (Record 버전)
 */
@Builder
public record IndexDataListResponse(
    Long id,
    LocalDate baseDate,         // 기준일자
    BigDecimal closingPrice,    // 종가
    BigDecimal versus,          // 대비
    BigDecimal fluctuationRate  // 등락률
) {}