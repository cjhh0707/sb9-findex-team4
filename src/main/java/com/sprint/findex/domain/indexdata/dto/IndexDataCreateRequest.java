package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 데이터 등록 요청 DTO (Record 버전)
 */
public record IndexDataCreateRequest(
        Long indexInfoId,               // 지수 정보 ID (매우 중요)
        LocalDate baseDate,             // 날짜
        SourceType sourceType,          // 소스 타입
        BigDecimal marketPrice,        // 시가
        BigDecimal closingPrice,        // 종가
        BigDecimal highPrice,           // 고가
        BigDecimal lowPrice,            // 저가
        BigDecimal versus,              // 대비
        BigDecimal fluctuationRate,     // 등락률
        Long tradingQuantity,           // 거래량
        Long tradingPrice,              // 거래 대금
        Long marketTotalAmount,      // 상장 시가 총액
        BigDecimal yearRecordHighPrice, // 연중 최고치
        String yearRecordHighDate,      // 연중 최고치 기록 일자
        BigDecimal yearRecordLowPrice,  // 연중 최저치
        String yearRecordLowDate        // 연중 최저치 기록 일자
) {}