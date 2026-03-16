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
}