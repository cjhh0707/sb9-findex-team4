package com.sprint.findex.domain.autointegration.repository;

import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoIntegrationRepository extends JpaRepository<AutoIntegration, Long> {

  // indexId 조회용
  Optional<AutoIntegration> findByIndexInfoId(Long indexInfoId);

  // 활성화(enabled) 상태 필터링
  // 예시: findAllByEnabled(true)
  java.util.List<AutoIntegration> findAllByEnabled(boolean enabled);

  @Query("""
SELECT a
FROM AutoIntegration a
WHERE (:indexInfoId IS NULL OR a.indexInfo.id = :indexInfoId)
AND (:enabled IS NULL OR a.enabled = :enabled)
AND (:idAfter IS NULL OR a.id > :idAfter)
ORDER BY a.id ASC
""")
  List<AutoIntegration> search(
      Long indexInfoId,
      Boolean enabled,
      Long idAfter,
      Pageable pageable
  );
}
