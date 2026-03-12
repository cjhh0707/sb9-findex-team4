package com.sprint.findex.domain.indexdata.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexDataCreateRequest {
  private LocalDate baseDate; // 날짜
  private SourceType sourceType; //소스 타입
  private BigDecimal openingPrice; // 시가
  private BigDecimal closingPrice; // 종가
  private BigDecimal highPrice; // 고가
  private BigDecimal lowPrice; // 저가
  private BigDecimal versus; // 대비
  private BigDecimal fluctuationRate; // 등략률

}
