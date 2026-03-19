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
import java.util.ArrayList;
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

        // ⭐ [수정 핵심] 상단 통계 수치를 위해 실제 데이터 개수를 카운트합니다.
        long totalElements = integrationRepository.countIntegrations(
                condition.getJobType(),
                condition.getIndexInfoId(),
                condition.getBaseDateFrom(),
                condition.getBaseDateTo(),
                condition.getWorker(),
                condition.getJobTimeFrom(),
                condition.getJobTimeTo(),
                jobResult
        );

        // 0L 대신 totalElements를 넘겨줘야 화면 상단 숫자가 올라갑니다!
        return new CursorPageResponse<>(
                content,
                nextIdAfter != null ? String.valueOf(nextIdAfter) : null,
                nextIdAfter,
                size,
                totalElements,
                hasNext
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 지수 정보 연동
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();

        // 지수별 이력 생성 (요구사항: "대상 지수가 여러 개인 경우 지수 별로 이력을 등록")
        List<Integration> savedJobs = allIndexInfos.stream().map(indexInfo ->
                integrationRepository.save(
                        integrationMapper.toEntity(indexInfo, JobType.INDEX_INFO,
                                LocalDate.now(), workerIp, JobResult.FAILED)
                )
        ).toList();

        // syncIndexInfo()는 단 한 번만 실행 (DB가 비어 있어도 실행)
        List<Long> jobIds = savedJobs.stream().map(Integration::getId).toList();
        this.executeOpenApiSyncInBackground(jobIds);

        return integrationMapper.toResponseList(savedJobs);
    }

    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(List<Long> jobIds) {
        log.info("[비동기] 지수 정보 연동 시작. Job 수: {}", jobIds.size());

        try {
            // ⭐ [핵심 추가] 공공데이터 API 서버에 과부하를 주지 않기 위해
            // 0.5초 정도 쉬었다가 시작하는 매너를 보여줍니다. (429 에러 방지)
            Thread.sleep(500);

            externalApiService.syncIndexInfo();

            // 성공 시 로그에 처리된 결과 개수를 남겨주면 디버깅이 훨씬 편해요!
            log.info("[비동기] 지수 정보 연동 완료. 대상 ID들: {}", jobIds);
            updateJobResults(jobIds, JobResult.SUCCESS);

        } catch (Exception e) {
            log.error("[비동기] 지수 정보 연동 실패. error: {}", e.getMessage());
            updateJobResults(jobIds, JobResult.FAILED);
        }
    }

    // ⭐ 중복 코드를 줄이기 위한 헬퍼 메서드
    private void updateJobResults(List<Long> jobIds, JobResult result) {
        for (Long jobId : jobIds) {
            integrationRepository.findById(jobId)
                    .ifPresent(job -> job.updateResult(result));
        }
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

        LocalDate toDate = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();
        LocalDate fromDate = request.getBaseDateFrom() != null ? request.getBaseDateFrom() : toDate;

        List<Integration> allSavedJobs = new ArrayList<>();

        for (IndexInfo indexInfo : targetInfos) {
            try {
                // API 호출 → 실제 데이터가 있는 날짜 목록 반환
                List<LocalDate> syncedDates = externalApiService.syncIndexData(indexInfo, fromDate, toDate);

                // 요구사항: "지수, 날짜 별로 이력을 등록" → 실제 거래일만큼 이력 생성
                if (syncedDates.isEmpty()) {
                    // 해당 기간에 데이터 없음 → FAILED 1건
                    allSavedJobs.add(integrationRepository.save(
                            integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA, toDate, workerIp, JobResult.FAILED)
                    ));
                } else {
                    for (LocalDate date : syncedDates) {
                        allSavedJobs.add(integrationRepository.save(
                                integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA, date, workerIp, JobResult.SUCCESS)
                        ));
                    }
                }
                log.info("[지수 데이터 연동 완료] 지수: {}, 기간: {} ~ {}, 이력 건수: {}",
                        indexInfo.getIndexName(), fromDate, toDate, syncedDates.size());
            } catch (Exception e) {
                log.error("[지수 데이터 연동 실패] 지수: {}, error: {}", indexInfo.getIndexName(), e.getMessage(), e);
                allSavedJobs.add(integrationRepository.save(
                        integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA, toDate, workerIp, JobResult.FAILED)
                ));
            }
        }

        return integrationMapper.toResponseList(allSavedJobs);
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
                integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA, to, worker, JobResult.FAILED)
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