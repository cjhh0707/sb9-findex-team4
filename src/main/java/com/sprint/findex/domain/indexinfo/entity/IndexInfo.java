package com.sprint.findex.domain.indexinfo.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.common.type.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
//@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IndexInfo extends BaseEntity {

  @Column(name = "idx_csf", nullable = false)
  private String idxCsf; //지수 분류명

  @Column(name = "idx_nm", nullable = false)
  private String idxNm; //지수명

  @Column(name = "epy_itms_cnt")
  private Integer epyItmsCnt; //채용 종목 수

  @Column(name = "bas_pntm")
  private String basPntm; //기준 시점

  @Column(name = "bas_idx")
  private BigDecimal basIdx; //기준 지수

  @Column(nullable = false)
  private Boolean favorite; //즐겨찾기 여부

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SourceType sourceType; //소스타입

  public void updateFavorite(Boolean favorite) {
    this.favorite = favorite;
  }

  public void updateInfo(Integer epyItmsCnt, String basPntm, BigDecimal basIdx){
    this.epyItmsCnt = epyItmsCnt;
    this.basPntm = basPntm;
    this.basIdx = basIdx;
  }


}
