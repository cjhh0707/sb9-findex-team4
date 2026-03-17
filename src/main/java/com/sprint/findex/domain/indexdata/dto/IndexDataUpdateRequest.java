package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;

/**
 * 지수 데이터 수정 요청 DTO
 * 기획서 사양: 지수, 날짜를 제외한 모든 속성 수정 가능
 */
public record IndexDataUpdateRequest(
    SourceType sourceType,      // (소스 타입: user / open-api)
    BigDecimal marketPrice,     // 시가
    BigDecimal closingPrice,    // 종가
    BigDecimal highPrice,       // 고가
    BigDecimal lowPrice,        // 저가
    BigDecimal versus,          // 대비
    BigDecimal fluctuationRate, //등락률
    Long tradingQuantity,       // 거래량
    Long tradingPrice,          // 거래대금
    Long marketTotalAmount      // 상장 시가 총액
) {}