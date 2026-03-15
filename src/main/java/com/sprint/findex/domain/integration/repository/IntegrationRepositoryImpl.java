package com.sprint.findex.domain.integration.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.sprint.findex.domain.integration.entity.QIntegration.integration;

@Repository
@RequiredArgsConstructor
public class IntegrationRepositoryImpl implements IntegrationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Integration> findIntegrationsByCondition(IntegrationSearchCondition condition) {
    return queryFactory
        .selectFrom(integration)
        .where(
            cursorCondition(condition.getIdAfter()), // 커서 페이징 조건
            jobTypeEq(condition.getJobType()),
            indexInfoIdEq(condition.getIndexInfoId()),
            targetDateBetween(condition.getBaseDateFrom(), condition.getBaseDateTo()),
            workerEq(condition.getWorker()),
            jobTimeBetween(condition.getJobTimeFrom(), condition.getJobTimeTo()),
            resultEq(condition.getStatus() != null ? JobResult.valueOf(condition.getStatus()) : null)
        )
        .orderBy(createOrderSpecifier(condition))
        .limit(condition.getSize() != null ? condition.getSize() : 10)
        .fetch();
  }

  // --- 동적 쿼리를 위한 BooleanExpression 조건 메서드들 ---

  private BooleanExpression cursorCondition(Long idAfter) {
    return idAfter != null ? integration.id.lt(idAfter) : null;
  }

  private BooleanExpression jobTypeEq(JobType jobType) {
    return jobType != null ? integration.jobType.eq(jobType) : null;
  }

  private BooleanExpression indexInfoIdEq(Long indexInfoId) {
    return indexInfoId != null ? integration.indexInfo.id.eq(indexInfoId) : null;
  }

  private BooleanExpression targetDateBetween(LocalDate from, LocalDate to) {
    if (from != null && to != null) return integration.targetDate.between(from, to);
    if (from != null) return integration.targetDate.goe(from);
    if (to != null) return integration.targetDate.loe(to);
    return null;
  }

  private BooleanExpression workerEq(String worker) {
    return worker != null ? integration.worker.eq(worker) : null;
  }

  private BooleanExpression jobTimeBetween(LocalDateTime from, LocalDateTime to) {
    if (from != null && to != null) return integration.jobTime.between(from, to);
    if (from != null) return integration.jobTime.goe(from);
    if (to != null) return integration.jobTime.loe(to);
    return null;
  }

  private BooleanExpression resultEq(JobResult status) {
    return status != null ? integration.result.eq(status) : null;
  }

  // --- 동적 정렬 조건 생성기 ---
  private OrderSpecifier<?> createOrderSpecifier(IntegrationSearchCondition condition) {
    String sortField = condition.getSortField();
    boolean isDesc = "desc".equalsIgnoreCase(condition.getSortDirection());

    if ("targetDate".equals(sortField)) {
      return isDesc ? integration.targetDate.desc() : integration.targetDate.asc();
    }
    return isDesc ? integration.jobTime.desc() : integration.jobTime.asc();
  }
}