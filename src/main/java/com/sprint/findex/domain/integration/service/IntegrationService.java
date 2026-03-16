package com.sprint.findex.domain.integration.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import com.sprint.findex.domain.integration.mapper.IntegrationMapper;
import com.sprint.findex.domain.integration.repository.IntegrationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationMapper integrationMapper;

    @Transactional(readOnly = true)
    public CursorPageResponse<IntegrationResponse> getIntegrations(IntegrationSearchCondition condition) {
        int size = condition.getSize() != null ? condition.getSize() : 10;

        // 1. Status 문자열을 JobResult Enum으로 안전하게 매핑 (FAILED -> FAIL)
        JobResult jobResult = null;
        if (condition.getStatus() != null && !condition.getStatus().isBlank()) {
            if ("FAILED".equalsIgnoreCase(condition.getStatus())) {
                jobResult = JobResult.FAIL;
            } else if ("SUCCESS".equalsIgnoreCase(condition.getStatus())) {
                jobResult = JobResult.SUCCESS;
            }
        }

        // 2. 동적 정렬(Sort) 생성
        String sortField = condition.getSortField() != null ? condition.getSortField() : "jobTime";
        String sortDirection = condition.getSortDirection() != null ? condition.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

        // 3. Pageable 객체 생성 (0페이지에서 size + 1 개 가져오기)
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        // 4. 레포지토리의 @Query 메서드 호출
        List<Integration> integrations = integrationRepository.searchIntegrations(
                condition.getIdAfter(),
                condition.getJobType(),
                condition.getIndexInfoId(),
                condition.getBaseDateFrom(),
                condition.getBaseDateTo(),
                condition.getWorker(),
                condition.getJobTimeFrom(),
                condition.getJobTimeTo(),
                jobResult,
                pageable
        );

        // 5. 다음 페이지 여부(hasNext) 판단 및 DTO 변환
        boolean hasNext = integrations.size() > size;

        List<IntegrationResponse> content = integrationMapper.toResponseList(
                integrations.stream().limit(size).toList()
        );

        Long nextIdAfter = null;
        if (!content.isEmpty()) {
            nextIdAfter = content.get(content.size() - 1).getId();
        }

        return new CursorPageResponse<>(
                content,
                null,
                nextIdAfter,
                size,
                null,
                hasNext
        );
    }

    // ===============================================
    // 아래 연동 비즈니스 로직들은 기존과 완벽하게 동일합니다.
    // ===============================================

    @Transactional
    public List<IntegrationResponse> createIndexDataSyncJob(IntegrationSyncRequest request, String workerIp) {
        log.info("[지수 데이터 연동 요청 접수] 작업자 IP: {}", workerIp);

        Integration newJob = integrationMapper.toEntity(
                null,
                JobType.INDEX_DATA,
                request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now(),
                workerIp,
                JobResult.NEW
        );
        Integration savedJob = integrationRepository.save(newJob);

        this.executeIndexDataSyncInBackground(savedJob.getId(), request);

        return integrationMapper.toResponseList(List.of(savedJob));
    }

    @Async
    @Transactional
    public void executeIndexDataSyncInBackground(Long jobId, IntegrationSyncRequest request) {
        log.info("[비동기 지수 데이터 연동 시작] Job ID: {}, 기간: {} ~ {}",
                jobId, request.getBaseDateFrom(), request.getBaseDateTo());

        try {
            // [TODO: 향후 ExternalApiService 를 주입받아 OpenAPI 통신 및 데이터 저장 로직 구현]

            integrationRepository.findById(jobId).ifPresent(job -> job.updateResult(JobResult.SUCCESS));
            log.info("[비동기 지수 데이터 연동 완료] Job ID: {}", jobId);

        } catch (Exception e) {
            log.error("[비동기 지수 데이터 연동 실패] 오류 발생: {}", e.getMessage(), e);
            integrationRepository.findById(jobId).ifPresent(job -> job.updateResult(JobResult.FAIL));
        }
    }

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        Integration newJob = integrationMapper.toEntity(
                null,
                JobType.INDEX_INFO,
                LocalDate.now(),
                workerIp,
                JobResult.NEW
        );
        Integration savedJob = integrationRepository.save(newJob);

        this.executeOpenApiSyncInBackground(savedJob.getId());

        return integrationMapper.toResponseList(List.of(savedJob));
    }

    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(Long jobId) {
        log.info("[백그라운드 작업 시작] 지수 정보 연동 (Job ID: {})", jobId);

        try {
            // [TODO: 향후 ExternalApiService 를 주입받아 OpenAPI 통신 및 데이터 저장 로직 구현]

            integrationRepository.findById(jobId).ifPresent(job -> job.updateResult(JobResult.SUCCESS));
            log.info("[백그라운드 작업 완료] 지수 정보 연동 성공");

        } catch (Exception e) {
            log.error("[백그라운드 작업 실패] 오류 발생: {}", e.getMessage(), e);
            integrationRepository.findById(jobId).ifPresent(job -> job.updateResult(JobResult.FAIL));
        }
    }
}