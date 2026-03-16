package com.sprint.findex.domain.indexdata.repository;

import com.sprint.findex.domain.indexdata.entity.IndexData;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

  /**
   * [목록 조회용] 필터링 + 커서 기반 No-offset 페이징 적용
   * * 팀장님 피드백 반영 사항:
   * 파라미터 명칭 변경: 기존 lastId를 API 명세서 표준인 idAfter로 통일하여 직관성 확보
   * 페이징 방식: Slice와 id < :idAfter 조건을 활용하여 성능이 최적화된 No-offset 방식 구현
   * 정렬: ID 내림차순(DESC) 정렬을 통해 가장 최신 데이터부터 순차적으로 커서 페이징 처리
   */
  @Query("SELECT i FROM IndexData i " +
      "WHERE (:indexInfoId IS NULL OR i.indexInfo.id = :indexInfoId) " +
      "AND (:startDate IS NULL OR i.baseDate >= :startDate) " +
      "AND (:endDate IS NULL OR i.baseDate <= :endDate) " +
      "AND (:idAfter IS NULL OR i.id < :idAfter) " + // lastId 대신 idAfter를 사용하여 커서 조건 적용
      "ORDER BY i.id DESC")
  Slice<IndexData> searchIndexData(
      @Param("indexInfoId") Long indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("idAfter") Long idAfter, // 명세서 규격에 맞춘 커서 파라미터
      Pageable pageable
  );

  /**
   * [CSV Export] 필터링은 동일하지만 페이징 없이 전체 리스트 조회
   * 기획서 사양: "페이지네이션은 고려하지 않습니다."
   */
  @Query("SELECT i FROM IndexData i " +
      "WHERE (:indexInfoId IS NULL OR i.indexInfo.id = :indexInfoId) " +
      "AND (:startDate IS NULL OR i.baseDate >= :startDate) " +
      "AND (:endDate IS NULL OR i.baseDate <= :endDate) " +
      "ORDER BY i.id DESC")
  List<IndexData> findAllForExport(
      @Param("indexInfoId") Long indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}