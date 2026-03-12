package com.sprint.findex.domain.indexinfo.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.common.type.SourceType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(
        name = "index_info",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"index_classification_name", "index_name"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IndexInfo extends BaseEntity {

  /** 지수 분류명 */
  @Column(name = "index_classification_name", length = 100, nullable = false)
  private String indexClassificationName;

  /** 지수명 */
  @Column(name = "index_name", length = 255, nullable = false)
  private String indexName;

  /** 채용 종목 수 */
  @Column(name = "employed_items_count")
  private Integer employedItemsCount;

  /** 기준 시점 */
  @Column(name = "base_point_in_time", length = 20)
  private String basePointInTime;

  /** 기준 지수 */
  @Column(name = "base_index")
  private BigDecimal baseIndex;

  /** 소스타입 */
  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", length = 20, nullable = false)
  private SourceType sourceType;

  /** 즐겨찾기 */
  @Column(nullable = false)
  private Boolean favorite;

  public void updateFavorite(Boolean favorite) {
    this.favorite = favorite;
  }

  public void updateInfo(Integer employedItemsCount, String basePointInTime, BigDecimal baseIndex) {
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
  }
}