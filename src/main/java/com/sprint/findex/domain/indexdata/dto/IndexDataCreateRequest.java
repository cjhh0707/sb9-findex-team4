package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexDataCreateRequest {
  private Long indexInfoId;      // 지수 정보 ID(매우 중요)
  private LocalDate baseDate; // 날짜
  private SourceType sourceType; //소스 타입
  private BigDecimal openingPrice; // 시가
  private BigDecimal closingPrice; // 종가
  private BigDecimal highPrice; // 고가
  private BigDecimal lowPrice; // 저가
  private BigDecimal versus; // 대비
  private BigDecimal fluctuationRate; // 등략률
  private Long tradingQuantity; // 거래량
  private Long tradingPrice; // 거래 대금
  private Long marketCapitalization; // 상장 시가 총액
  private BigDecimal yearRecordHighPrice; // 연중 최고치
  private String yearRecordHighDate;      // 연중 최고치 기록 일자
  private BigDecimal yearRecordLowPrice;  // 연중 최저치
  private String yearRecordLowDate;       // 연중 최저치 기록 일자

}
