package com.sprint.findex.domain.indexdata.repository;

import com.sprint.findex.domain.indexdata.entity.IndexData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

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
}