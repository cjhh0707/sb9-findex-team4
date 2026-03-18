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

  /**지수 분류명과 지수명으로 중복 여부를 확인하는 기본 제공 메서드(등록 시 유효성 검사용)*/
  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

  //대시보드(관심 지수 성과 조회)를 위한 추가 메서드
  List<IndexInfo> findAllByFavoriteTrue();

  // OpenAPI 연동: 분류명 + 지수명으로 단건 조회
  Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);

}