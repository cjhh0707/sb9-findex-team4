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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", length = 20, nullable = false)
  private SourceType sourceType;

  @Column(name = "opening_price")
  private BigDecimal openingPrice;

  @Column(name = "closing_price")
  private BigDecimal closingPrice;

  @Column(name = "high_price")
  private BigDecimal highPrice;

  @Column(name = "low_price")
  private BigDecimal lowPrice;

  @Column(name = "versus")
  private BigDecimal versus;

  @Column(name = "fluctuation_rate")
  private BigDecimal fluctuationRate;

  @Column(name = "trading_quantity")
  private Long tradingQuantity;

  @Column(name = "trading_price")
  private Long tradingPrice;

  @Column(name = "market_capitalization")
  private Long marketCapitalization;
}