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
    LocalDate baseDate,             // 날짜
    SourceType sourceType,          // 출처
    BigDecimal openingPrice,        // 시가
    BigDecimal closingPrice,        // 종가
    BigDecimal highPrice,           // 고가
    BigDecimal lowPrice,            // 저가
    BigDecimal versus,              // 대비
    BigDecimal fluctuationRate,     // 등락률
    Long tradingQuantity,           // 거래량
    Long tradingPrice,              // 거래 대금
    Long marketCapitalization,      // 상장 시가 총액
    BigDecimal yearRecordHighPrice, // 연중 최고치
    String yearRecordHighDate,      // 연중 최고치 기록 일자
    BigDecimal yearRecordLowPrice,  // 연중 최저치
    String yearRecordLowDate        // 연중 최저치 기록 일자
) {}