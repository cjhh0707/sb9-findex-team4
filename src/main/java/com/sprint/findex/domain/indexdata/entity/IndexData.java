package com.sprint.findex.domain.indexdata.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.common.type.SourceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData extends BaseEntity {

  // IndexInfo와의 관계 설정은 상의 후 추가

  private LocalDate baseDate;

  @Enumerated(EnumType.STRING) // db에 글자관리가 편해짐
  private SourceType sourceType;

  private BigDecimal openingPrice;
  private BigDecimal closingPrice;
  private BigDecimal highPrice;
  private BigDecimal lowPrice;
  private BigDecimal versus;
  private BigDecimal fluctuationRate;

  private Long tradingQuantity;
  private Long tradingPrice;
  private Long marketCapitalization;
}