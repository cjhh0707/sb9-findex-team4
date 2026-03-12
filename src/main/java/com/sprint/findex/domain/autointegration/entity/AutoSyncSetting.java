package com.sprint.findex.domain.autointegration.entity;


import com.sprint.findex.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auto_sync_setting", uniqueConstraints = {
    @UniqueConstraint(name = "uk_index_id", columnNames = "index_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoSyncSetting extends BaseEntity { // 생성 시간 업데이트 시간 상속 (자동 관리)

  @Column(name = "index_id", nullable = false, unique = true)
  private Long indexInfoId;

//  기본값 false
  @Builder.Default
  @Column(nullable = false)
  private boolean enabled = false;

  @Column(name = "last_sync_date")
  private LocalDateTime lastSyncDate;
}


