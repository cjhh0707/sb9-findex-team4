package com.sprint.findex.domain.integration.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import com.sprint.findex.domain.integration.mapper.IntegrationMapper;
import com.sprint.findex.domain.integration.repository.IntegrationRepository;
import com.sprint.findex.domain.openapi.ExternalApiService;
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
    private final IndexInfoRepository indexInfoRepository;
    private final ExternalApiService externalApiService;

    // ──────────────────────────────────────────────────────────────────────────
    // 연동 작업 목록 조회
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CursorPageResponse<IntegrationResponse> getIntegrations(IntegrationSearchCondition condition) {
        int size = condition.getSize() != null ? condition.getSize() : 10;

        JobResult jobResult = null;
        if (condition.getStatus() != null && !condition.getStatus().isBlank()) {
            try {
                jobResult = JobResult.valueOf(condition.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String sortField = condition.getSortField() != null ? condition.getSortField() : "jobTime";
        String sortDirection = condition.getSortDirection() != null ? condition.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(0, size + 1, sort);

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

        boolean hasNext = integrations.size() > size;

        List<IntegrationResponse> content = integrationMapper.toResponseList(
                integrations.stream().limit(size).toList()
        );

        Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).getId();

        // ⭐ [수정] 다섯 번째 인자(totalElements)를 null 대신 0L로 변경하여 프론트엔드 에러 방지
        return new CursorPageResponse<>(content, null, nextIdAfter, size, 0L, hasNext);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 지수 정보 연동
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();

        // 지수별 이력 생성 (요구사항: "대상 지수가 여러 개인 경우 지수 별로 이력을 등록")
        // DB에 지수가 없으면 빈 리스트 반환 (API 연동으로 신규 지수가 생성되나 이력은 기존 지수 기준으로만 남김)
        List<Integration> savedJobs = allIndexInfos.stream().map(indexInfo ->
                integrationRepository.save(
                        integrationMapper.toEntity(indexInfo, JobType.INDEX_INFO,
                                LocalDate.now(), workerIp, JobResult.NEW)
                )
        ).toList();

        savedJobs.forEach(j -> this.executeOpenApiSyncInBackground(j.getId()));
        return integrationMapper.toResponseList(savedJobs);
    }

    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(Long jobId) {
        log.info("[비동기] 지수 정보 연동 시작. Job ID: {}", jobId);
        integrationRepository.findById(jobId).ifPresent(job -> {
            try {
                externalApiService.syncIndexInfo();
                job.updateResult(JobResult.SUCCESS);
                log.info("[비동기] 지수 정보 연동 완료. Job ID: {}", jobId);
            } catch (Exception e) {
                log.error("[비동기] 지수 정보 연동 실패. Job ID: {}, error: {}", jobId, e.getMessage(), e);
                job.updateResult(JobResult.FAILED);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 지수 데이터 연동
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<IntegrationResponse> createIndexDataSyncJob(IntegrationSyncRequest request, String workerIp) {
        log.info("[지수 데이터 연동 요청 접수] 작업자 IP: {}, 기간: {} ~ {}",
                workerIp, request.getBaseDateFrom(), request.getBaseDateTo());

        List<IndexInfo> targetInfos;
        List<Long> ids = request.getIndexInfoIds();
        if (ids != null && !ids.isEmpty()) {
            targetInfos = indexInfoRepository.findAllById(ids);
        } else {
            targetInfos = indexInfoRepository.findAll();
        }

        LocalDate targetDate = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();

        // 지수별로 Integration 이력 생성 (요구사항: "지수, 날짜 별로 이력을 등록")
        List<Integration> savedJobs = targetInfos.stream().map(indexInfo ->
                integrationRepository.save(
                        integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA,
                                targetDate, workerIp, JobResult.NEW)
                )
        ).toList();

        savedJobs.forEach(job -> this.executeIndexDataSyncInBackground(job.getId(), request));

        return integrationMapper.toResponseList(savedJobs);
    }

    @Async
    @Transactional
    public void executeIndexDataSyncInBackground(Long jobId, IntegrationSyncRequest request) {
        log.info("[비동기] 지수 데이터 연동 시작. Job ID: {}, 기간: {} ~ {}",
                jobId, request.getBaseDateFrom(), request.getBaseDateTo());

        integrationRepository.findById(jobId).ifPresent(job -> {
            try {
                IndexInfo indexInfo = job.getIndexInfo();
                LocalDate from = request.getBaseDateFrom();
                LocalDate to = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();

                externalApiService.syncIndexData(indexInfo, from, to);
                job.updateResult(JobResult.SUCCESS);
                log.info("[비동기] 지수 데이터 연동 완료. Job ID: {}", jobId);
            } catch (Exception e) {
                log.error("[비동기] 지수 데이터 연동 실패. Job ID: {}, error: {}", jobId, e.getMessage(), e);
                job.updateResult(JobResult.FAILED);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 배치(자동 연동 스케줄러)에서 호출하는 메서드
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void runBatchSync(IndexInfo indexInfo, LocalDate from, LocalDate to, String worker) {
        log.info("[배치 연동] 지수: {}, 기간: {} ~ {}", indexInfo.getIndexName(), from, to);

        Integration job = integrationRepository.save(
                integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA, to, worker, JobResult.NEW)
        );

        try {
            externalApiService.syncIndexData(indexInfo, from, to);
            job.updateResult(JobResult.SUCCESS);
            log.info("[배치 연동 완료] 지수: {}", indexInfo.getIndexName());
        } catch (Exception e) {
            log.error("[배치 연동 실패] 지수: {}, error: {}", indexInfo.getIndexName(), e.getMessage(), e);
            job.updateResult(JobResult.FAILED);
        }
    }
}
