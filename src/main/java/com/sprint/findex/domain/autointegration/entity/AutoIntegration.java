package com.sprint.findex.domain.autointegration.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "auto_integration",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"index_info_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AutoIntegration extends BaseEntity {

  /** IndexInfo */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  /** 활성화 여부 */
  @Builder.Default
  @Column(nullable = false)
  private Boolean enabled = false;
}