package com.sprint.findex.domain.indexinfo.repository;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
  @Query("""
        SELECT i FROM IndexInfo i
        WHERE (:indexClassification IS NULL OR i.indexClassification LIKE CONCAT('%', :indexClassification, '%'))
          AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
          AND (:favorite IS NULL OR i.favorite = :favorite)
        """)
  List<IndexInfo> searchIndexInfos(
          @Param("indexClassification") String indexClassification,
          @Param("indexName") String indexName,
          @Param("favorite") Boolean favorite,
          Pageable pageable
  );
  @Query("""
        SELECT COUNT(i) FROM IndexInfo i
        WHERE (:indexClassification IS NULL OR i.indexClassification LIKE CONCAT('%', :indexClassification, '%'))
          AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
          AND (:favorite IS NULL OR i.favorite = :favorite)
        """)
  long countIndexInfos(
          @Param("indexClassification") String indexClassification,
          @Param("indexName") String indexName,
          @Param("favorite") Boolean favorite
  );

  // 중복 여부 확인 (등록 시 유효성 검사)
  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

  // 즐겨찾기 지수 조회 (대시보드용)
  List<IndexInfo> findAllByFavoriteTrue();

  // 분류명 + 지수명으로 단건 조회
  Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);

}