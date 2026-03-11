package com.sprint.findex.domain.indexdata;

import com.sprint.findex.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 함부로 생성하지 못하게 막는 팀 프로젝트 국룰!
public class IndexData extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // index_info_id (FK) 부분은 나중에 IndexInfo 엔티티가 만들어지면 연결할 거예요.
  // 지금은 우선 필드들만 먼저 선언해둡니다.

  @Column(nullable = false)
  private LocalDate baseDate; // 날짜

  @Column(nullable = false)
  private String sourceType; // 소스 타입 (user / open-api)

  private BigDecimal openingPrice;    // 시가
  private BigDecimal closingPrice;    // 종가
  private BigDecimal highPrice;       // 고가
  private BigDecimal lowPrice;        // 저가
  private BigDecimal versus;          // 대비
  private BigDecimal fluctuationRate; // 등락률

  private Long tradingQuantity;       // 거래량
  private Long tradingPrice;          // 거래대금
  private Long marketCapitalization;  // 상장 시가 총액
}