package com.sprint.findex.domain.autointegration.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Builder.Default
  @Column(nullable = false)
  private Boolean enabled = false;

  public void changeEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Column(nullable = true)
  private LocalDateTime lastSyncAt;


  public void updateLastIntegrationAt(LocalDateTime lastSyncAt) {
    this.lastSyncAt = lastSyncAt;
  }
}