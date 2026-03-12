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
}