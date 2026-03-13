package com.sprint.findex.domain.autointegration.repository;

import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long> {

  // indexId 조회용
  Optional<AutoIntegration> findByIndexInfoId(Long indexInfoId);

  // 활성화(enabled) 상태 필터링
  // 예시: findAllByEnabled(true)
  java.util.List<AutoIntegration> findAllByEnabled(boolean enabled);

}
