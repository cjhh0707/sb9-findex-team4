package com.sprint.findex.domain.indexdata.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.common.type.SourceType;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
        name = "index_data",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"index_info_id", "base_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IndexData extends BaseEntity {

  /** 지수 정보 (부모) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  /** 날짜 */
  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  /** 소스 타입 */
  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", length = 20, nullable = false)
  private SourceType sourceType;

  /** 시가 */
  @Column(name = "opening_price")
  private BigDecimal openingPrice;

  /** 종가 */
  @Column(name = "closing_price")
  private BigDecimal closingPrice;

  /** 고가 */
  @Column(name = "high_price")
  private BigDecimal highPrice;

  /** 저가 */
  @Column(name = "low_price")
  private BigDecimal lowPrice;

  /** 대비 */
  @Column(name = "versus")
  private BigDecimal versus;

  /** 등락률 */
  @Column(name = "fluctuation_rate")
  private BigDecimal fluctuationRate;

  /** 거래량 */
  @Column(name = "trading_quantity")
  private Long tradingQuantity;

  /** 거래 대금 */
  @Column(name = "trading_price")
  private Long tradingPrice;

  /** 상장 시가 총액 */
  @Column(name = "market_capitalization")
  private Long marketCapitalization;

  /** 연중 최고치 */
  @Column(name = "year_record_high_price")
  private BigDecimal yearRecordHighPrice;

  /** 연중 최고치 기록 일자 */
  @Column(name = "year_record_high_date")
  private String yearRecordHighDate;

  /** 연중 최저치 */
  @Column(name = "year_record_low_price")
  private BigDecimal yearRecordLowPrice;

  /** 연중 최저치 기록 일자 */
  @Column(name = "year_record_low_date")
  private String yearRecordLowDate;

  /**
   * [추가] 기획서에 따라 지수와 날짜를 제외한 모든 속성을 수정합니다.
   */
  public void update(SourceType sourceType, BigDecimal openingPrice, BigDecimal closingPrice,
      BigDecimal highPrice, BigDecimal lowPrice, BigDecimal versus,
      BigDecimal fluctuationRate, Long tradingQuantity, Long tradingPrice,
      Long marketCapitalization, BigDecimal yearRecordHighPrice,
      String yearRecordHighDate, BigDecimal yearRecordLowPrice,
      String yearRecordLowDate) {
    this.sourceType = sourceType;
    this.openingPrice = openingPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.tradingPrice = tradingPrice;
    this.marketCapitalization = marketCapitalization;
    this.yearRecordHighPrice = yearRecordHighPrice;
    this.yearRecordHighDate = yearRecordHighDate;
    this.yearRecordLowPrice = yearRecordLowPrice;
    this.yearRecordLowDate = yearRecordLowDate;
  }
}