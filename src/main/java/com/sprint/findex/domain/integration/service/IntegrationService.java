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

        // ⭐ 1. [여기에 추가] 진짜 총 개수를 세는 코드 한 줄 추가!
        long totalCount = 0L;
        if (jobResult != null) {
            // 성공(SUCCESS)이나 실패(FAILED) 조건이 있으면 그것만 센다
            totalCount = integrationRepository.countByResult(jobResult);
        } else {
            // 조건이 없으면 전체 개수를 센다
            totalCount = integrationRepository.count();
        }

        // ⭐ 2. [여기만 수정] 맨 마지막 0L을 totalCount로 변경!
        return new CursorPageResponse<>(content, null, nextIdAfter, size, totalCount, hasNext);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 지수 정보 연동 (수정된 버전)
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();

        // 1. 요구사항대로 166개의 이력(NEW)을 먼저 DB에 생성합니다.
        List<Integration> savedJobs = allIndexInfos.stream().map(indexInfo ->
            integrationRepository.save(
                integrationMapper.toEntity(indexInfo, JobType.INDEX_INFO,
                    LocalDate.now(), workerIp, JobResult.NEW)
            )
        ).toList();

        // ⭐ 2. [핵심 수정] 166명을 각자 보내지 않고, 'Job ID 166개가 적힌 리스트'를
        // 딱 1명의 비동기 배달원에게 쥐여주고 출발시킵니다!
        List<Long> jobIds = savedJobs.stream().map(Integration::getId).toList();
        this.executeOpenApiSyncInBackground(jobIds);

        return integrationMapper.toResponseList(savedJobs);
    }

    // ⭐ 3. [핵심 수정] Long jobId 하나만 받던 것을 List<Long>을 받도록 수정합니다.
    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(List<Long> jobIds) {
        log.info("[비동기] 지수 정보 일괄 연동 시작. 대상 이력 총 {}건", jobIds.size());
        try {
            // 딱 1번만 API 통신을 해서 166개를 한꺼번에 업데이트합니다! (서버 과부하 0%)
            externalApiService.syncIndexInfo();

            // API 통신이 성공하면, 166개의 이력을 한 번에 SUCCESS로 바꿔줍니다.
            List<Integration> jobs = integrationRepository.findAllById(jobIds);
            jobs.forEach(job -> job.updateResult(JobResult.SUCCESS));
            log.info("[비동기] 지수 정보 일괄 연동 완료.");

        } catch (Exception e) {
            log.error("[비동기] 지수 정보 일괄 연동 실패. error: {}", e.getMessage(), e);
            List<Integration> jobs = integrationRepository.findAllById(jobIds);
            jobs.forEach(job -> job.updateResult(JobResult.FAILED));
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

        // ⭐ [절대 뚫리지 않는 방어막] 프론트가 null, 0, 빈 값 등 무엇을 보내든 완벽하게 판별합니다.
        boolean isSearchAll = (ids == null || ids.isEmpty() || ids.contains(0L) || ids.contains(null));

        if (!isSearchAll) {
            // 특정 지수 ID로 검색 시도
            targetInfos = indexInfoRepository.findAllById(ids);

            // 만약 서버를 껐다 켜서 프론트가 기억하는 옛날 ID가 DB에서 다 날아갔다면?
            if (targetInfos.isEmpty()) {
                log.warn("요청받은 ID로 지수를 찾지 못해 전체 지수를 연동합니다.");
                targetInfos = indexInfoRepository.findAll();
            }
        } else {
            // '전체 지수'인 경우
            targetInfos = indexInfoRepository.findAll();
        }

        LocalDate targetDate = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();

        // 166개의 이력 만들기
        List<Integration> savedJobs = targetInfos.stream().map(indexInfo ->
            integrationRepository.save(
                integrationMapper.toEntity(indexInfo, JobType.INDEX_DATA,
                    targetDate, workerIp, JobResult.NEW)
            )
        ).toList();

        // 1개의 스레드에게 jobIds 리스트 쥐여주고 출발!
        List<Long> jobIds = savedJobs.stream().map(Integration::getId).toList();
        this.executeIndexDataSyncInBackground(jobIds, request);

        return integrationMapper.toResponseList(savedJobs);
    }

    // ⭐ [핵심 수정 2] List<Long>을 받고, 장시간 DB 잠금을 막기 위해 @Transactional을 제거합니다.
    @Async
    public void executeIndexDataSyncInBackground(List<Long> jobIds, IntegrationSyncRequest request) {
        log.info("[비동기] 지수 데이터 일괄 연동 시작. 총 {}개 지수", jobIds.size());

        for (Long jobId : jobIds) {
            try {
                // 각각의 작업마다 DB를 조회해서 처리합니다.
                integrationRepository.findById(jobId).ifPresent(job -> {
                    IndexInfo indexInfo = job.getIndexInfo();
                    LocalDate from = request.getBaseDateFrom();
                    LocalDate to = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();

                    // 외부 API 통신
                    externalApiService.syncIndexData(indexInfo, from, to);

                    // 성공 처리 후 명시적 저장
                    job.updateResult(JobResult.SUCCESS);
                    integrationRepository.save(job);
                    log.info("[비동기] 연동 완료: {}", indexInfo.getIndexName());
                });

                // ⭐ [가장 중요한 방어막] 공공데이터 API 서버가 놀라지 않도록 1건 처리할 때마다 0.5초씩 대기합니다!
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("[비동기] 연동 실패. Job ID: {}, error: {}", jobId, e.getMessage());
                integrationRepository.findById(jobId).ifPresent(job -> {
                    job.updateResult(JobResult.FAILED);
                    integrationRepository.save(job);
                });
            }
        }
        log.info("[비동기] 지수 데이터 일괄 연동 100% 완료!");
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
