package com.sprint.findex.domain.indexinfo.repository;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
  /**
   * @Query를 활용한 동적 검색 및 No-offset 페이징 메서드입니다.
   * * [동적 쿼리 처리 방식]
   * :파라미터명 IS NULL 구문을 사용하여, 검색어가 안 들어오면 해당 조건을 무시하고 전체를 찾습니다.
   * * [부분/완전 일치 처리]
   * - LIKE %:파라미터%: 부분 일치 (지수 분류명, 지수명)
   * - = :파라미터: 완전 일치 (즐겨찾기)
   * * [No-offset 페이징]
   * - i.id < :lastId : 이전 페이지의 마지막 ID보다 작은(과거) 데이터만 가져옵니다.
   * (최초 1페이지 조회 시에는 lastId가 null로 들어오므로 이 조건이 무시됩니다)
   */
  @Query("""
        SELECT i FROM IndexInfo i
        WHERE (:indexClassificationName IS NULL OR i.indexClassificationName LIKE CONCAT('%', :indexClassificationName, '%'))
          AND (:indexName IS NULL OR i.indexName LIKE CONCAT('%', :indexName, '%'))
          AND (:favorite IS NULL OR i.favorite = :favorite)
          AND (:lastId IS NULL OR i.id < :lastId)
        ORDER BY i.id DESC
        """)
  List<IndexInfo> searchIndexInfos(
      @Param("indexClassificationName") String indexClassificationName,
      @Param("indexName") String indexName,
      @Param("favorite") Boolean favorite,
      @Param("lastId") Long lastId,
      Pageable pageable
  );
  /**지수 분류명과 지수명으로 중복 여부를 확인하는 기본 제공 메서드(등록 시 유효성 검사용)*/
  boolean existsByIndexClassificationNameAndIndexName(String indexClassificationName, String indexName);

}