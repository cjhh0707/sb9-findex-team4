package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 데이터 상세 응답 DTO (Record 버전)
 */
@Builder
public record IndexDataResponse(
    Long id,                        // 데이터 고유 번호
    Long indexInfoId,
    LocalDate baseDate,             // 날짜
    SourceType sourceType,          // 출처
    BigDecimal marketPrice,        // 시가
    BigDecimal closingPrice,        // 종가
    BigDecimal highPrice,           // 고가
    BigDecimal lowPrice,            // 저가
    BigDecimal versus,              // 대비
    BigDecimal fluctuationRate,     // 등락률
    Long tradingQuantity,           // 거래량
    Long tradingPrice,              // 거래 대금
    Long marketTotalAmount          // 상장 시가 총액
) {}