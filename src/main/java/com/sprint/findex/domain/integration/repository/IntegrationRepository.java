package com.sprint.findex.domain.integration.repository;

import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    // QueryDSL을 대체하는 순수 JPQL 동적 쿼리
    @Query("SELECT i FROM Integration i " +
            "WHERE (:idAfter IS NULL OR i.id < :idAfter) " +
            "AND (:jobType IS NULL OR i.jobType = :jobType) " +
            "AND (:indexInfoId IS NULL OR i.indexInfo.id = :indexInfoId) " +
            "AND (CAST(:baseDateFrom AS date) IS NULL OR i.targetDate >= :baseDateFrom) " +
            "AND (CAST(:baseDateTo AS date) IS NULL OR i.targetDate <= :baseDateTo) " +
            "AND (:worker IS NULL OR i.worker LIKE %:worker%) " +
            "AND (CAST(:jobTimeFrom AS timestamp) IS NULL OR i.jobTime >= :jobTimeFrom) " +
            "AND (CAST(:jobTimeTo AS timestamp) IS NULL OR i.jobTime <= :jobTimeTo) " +
            "AND (:result IS NULL OR i.result = :result)")
    List<Integration> searchIntegrations(
            @Param("idAfter") Long idAfter,
            @Param("jobType") JobType jobType,
            @Param("indexInfoId") Long indexInfoId,
            @Param("baseDateFrom") LocalDate baseDateFrom,
            @Param("baseDateTo") LocalDate baseDateTo,
            @Param("worker") String worker,
            @Param("jobTimeFrom") LocalDateTime jobTimeFrom,
            @Param("jobTimeTo") LocalDateTime jobTimeTo,
            @Param("result") JobResult result,
            Pageable pageable
    );

    /**
     * 자동 연동 스케줄러용: 특정 지수의 마지막 성공 targetDate 조회
     * worker="system" 조건으로 배치 실행 기록만 필터링
     */
    @Query("SELECT MAX(i.targetDate) FROM Integration i " +
            "WHERE i.indexInfo.id = :indexInfoId " +
            "AND i.jobType = :jobType " +
            "AND i.worker = 'system' " +
            "AND i.result = :result")
    Optional<LocalDate> findLastSuccessTargetDate(
            @Param("indexInfoId") Long indexInfoId,
            @Param("jobType") JobType jobType,
            @Param("result") JobResult result
    );
}