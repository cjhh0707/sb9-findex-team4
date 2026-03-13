package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexDataResponse {
  private final Long id;           // 데이터 고유 번호
  private final LocalDate baseDate; // 날짜
  private final SourceType sourceType; // 출처
  private final BigDecimal openingPrice; // 시가
  private final BigDecimal closingPrice; // 종가
  private final BigDecimal highPrice;    // 고가
  private final BigDecimal lowPrice;     // 저가
  private final BigDecimal versus;       // 대비
  private final BigDecimal fluctuationRate; // 등락률
  private final Long tradingQuantity; // 거래량
  private final Long tradingPrice; // 거래 대금
  private final Long marketCapitalization; // 상장 시가 총액
  private final BigDecimal yearRecordHighPrice; // 연중 최고치
  private final String yearRecordHighDate;      // 연중 최고치 기록 일자
  private final BigDecimal yearRecordLowPrice;  // 연중 최저치
  private final String yearRecordLowDate;       // 연중 최저치 기록 일자
}