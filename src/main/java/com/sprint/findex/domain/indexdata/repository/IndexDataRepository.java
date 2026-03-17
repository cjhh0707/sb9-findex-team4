package com.sprint.findex.domain.indexdata.repository;

import com.sprint.findex.domain.indexdata.entity.IndexData;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

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
      "AND (:idAfter IS NULL OR i.id > :idAfter) ") // lastId 대신 idAfter를 사용하여 커서 조건 적용
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
          "AND (:endDate IS NULL OR i.baseDate <= :endDate) ") // ORDER BY i.id DESC 삭제! Sort 파라미터가 알아서 해줍니다.
  List<IndexData> findAllForExport(
          @Param("indexInfoId") Long indexInfoId,
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate,
          Sort sort // 정렬 파라미터 추가
  );

  // OpenAPI 연동용: (지수 ID + 날짜) 단건 조회 (upsert 판별)
  Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

  // 특정 지수의 가장 최신 데이터 1개 조회
  Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long indexInfoId);

  // 특정 지수의 특정 기간 데이터 오름차순 조회 (차트용)
  List<IndexData> findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate start, LocalDate end);

  // 지수 ID 리스트를 받아, 각 지수별로 maxDate 이하의 '가장 최신 날짜' 데이터를 가져오는 쿼리
  @Query("SELECT d FROM IndexData d WHERE d.baseDate = " +
          "(SELECT MAX(d2.baseDate) FROM IndexData d2 WHERE d2.indexInfo.id = d.indexInfo.id AND d2.baseDate <= :maxDate) " +
          "AND d.indexInfo.id IN :idList")
  List<IndexData> findMostRecentByIndexInfoIdsAndMaxDate(@Param("idList") List<Long> idList, @Param("maxDate") LocalDate maxDate);

  // 지수 ID 리스트를 받아, 각 지수별로 targetDate 이하, limitDate 이상인 '가장 최신 날짜(가장 가까운 과거)' 데이터를 가져오는 쿼리
  @Query("SELECT d FROM IndexData d WHERE d.baseDate = " +
          "(SELECT MAX(d2.baseDate) FROM IndexData d2 WHERE d2.indexInfo.id = d.indexInfo.id AND d2.baseDate <= :targetDate AND d2.baseDate >= :limitDate) " +
          "AND d.indexInfo.id IN :idList")
  List<IndexData> findClosestPastByIndexInfoIdsAndTargetDate(@Param("idList") List<Long> idList, @Param("targetDate") LocalDate targetDate, @Param("limitDate") LocalDate limitDate);
}