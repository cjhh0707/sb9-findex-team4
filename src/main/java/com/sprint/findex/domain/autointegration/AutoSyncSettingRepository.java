package com.sprint.findex.domain.autointegration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutoSyncSettingRepository extends JpaRepository<AutoSyncSetting, UUID> {

  // indexId 조회용
  Optional<AutoSyncSetting> findByIndexId(String indexId);

  // 활성화(enabled) 상태 필터링
  // 예시: findAllByEnabled(true)
  java.util.List<AutoSyncSetting> findAllByEnabled(boolean enabled);

}
