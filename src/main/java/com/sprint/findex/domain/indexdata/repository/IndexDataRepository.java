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
   * [목록 조회용] 필터링 + No-offset 페이징 적용
   */
  @Query("SELECT i FROM IndexData i " +
      "WHERE (:indexInfoId IS NULL OR i.indexInfo.id = :indexInfoId) " +
      "AND (:startDate IS NULL OR i.baseDate >= :startDate) " +
      "AND (:endDate IS NULL OR i.baseDate <= :endDate) " +
      "AND (:lastId IS NULL OR i.id < :lastId) " + // No-offset 페이징 조건
      "ORDER BY i.id DESC") // 최신순 정렬 예시
  Slice<IndexData> searchIndexData(
      @Param("indexInfoId") Long indexInfoId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("lastId") Long lastId,
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