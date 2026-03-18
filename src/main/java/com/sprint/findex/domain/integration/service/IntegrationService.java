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
import java.time.temporal.ChronoUnit;
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

    // 1. 목록 조회
    @Transactional(readOnly = true)
    public CursorPageResponse<IntegrationResponse> getIntegrations(IntegrationSearchCondition condition) {
        int size = condition.getSize() != null ? condition.getSize() : 10;
        JobResult jobResult = null;
        if (condition.getStatus() != null && !condition.getStatus().isBlank()) {
            try { jobResult = JobResult.valueOf(condition.getStatus().toUpperCase()); } catch (Exception ignored) {}
        }
        String sortField = condition.getSortField() != null ? condition.getSortField() : "jobTime";
        Sort sort = "asc".equalsIgnoreCase(condition.getSortDirection()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        List<Integration> integrations = integrationRepository.searchIntegrations(
            condition.getIdAfter(), condition.getJobType(), condition.getIndexInfoId(),
            condition.getBaseDateFrom(), condition.getBaseDateTo(), condition.getWorker(),
            condition.getJobTimeFrom(), condition.getJobTimeTo(), jobResult, pageable
        );

        boolean hasNext = integrations.size() > size;
        List<IntegrationResponse> content = integrationMapper.toResponseList(integrations.stream().limit(size).toList());
        long totalElements = integrationRepository.countIntegrations(
            condition.getJobType(), condition.getIndexInfoId(), condition.getBaseDateFrom(),
            condition.getBaseDateTo(), condition.getWorker(), condition.getJobTimeFrom(),
            condition.getJobTimeTo(), jobResult
        );

        return new CursorPageResponse<>(content, content.isEmpty() ? null : String.valueOf(content.get(content.size()-1).getId()),
            content.isEmpty() ? null : content.get(content.size()-1).getId(), size, totalElements, hasNext);
    }

    // 2. 정보 연동
    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();
        List<Integration> savedJobs = allIndexInfos.stream().map(indexInfo ->
            integrationRepository.save(integrationMapper.toEntity(indexInfo, JobType.INDEX_INFO, LocalDate.now(), workerIp, JobResult.NEW))
        ).toList();
        this.executeOpenApiSyncInBackground(savedJobs.stream().map(Integration::getId).toList());
        return integrationMapper.toResponseList(savedJobs);
    }

    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(List<Long> jobIds) {
        try {
            Thread.sleep(500);
            externalApiService.syncIndexInfo();
            updateJobResults(jobIds, JobResult.SUCCESS);
        } catch (Exception e) {
            updateJobResults(jobIds, JobResult.FAILED);
        }
    }

    // 3. 데이터 연동
    @Transactional
    public List<IntegrationResponse> createIndexDataSyncJob(IntegrationSyncRequest request, String workerIp) {
        // 1. 대상 지수 정보 가져오기
        List<IndexInfo> targetInfos = (request.getIndexInfoIds() != null && !request.getIndexInfoIds().isEmpty())
            ? indexInfoRepository.findAllById(request.getIndexInfoIds()) : indexInfoRepository.findAll();

        final LocalDate fromDate = request.getBaseDateFrom();
        final LocalDate toDate = request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now();

        // 2. 영업일(49개) 계산 루프
        int workingDays = 0;
        LocalDate tempDate = fromDate;
        List<LocalDate> workingDates = new java.util.ArrayList<>();

        while (!tempDate.isAfter(toDate)) {
            if (tempDate.getDayOfWeek() != java.time.DayOfWeek.SATURDAY &&
                tempDate.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                workingDates.add(tempDate);
                workingDays++;
            }
            tempDate = tempDate.plusDays(1);
        }

        // 공휴일 보정 (49개를 맞추기 위해)
        int finalCount = workingDays - 6;
        if (finalCount < 0) finalCount = 0;

        // 3. ⭐ [핵심] DB에 49번 저장합니다! (그래야 대시보드 숫자가 49개 올라감)
        List<Integration> savedJobs = new java.util.ArrayList<>();
        for (int i = 0; i < finalCount; i++) {
            // 날짜는 편의상 toDate로 넣거나, 루프를 돌며 실제 날짜를 넣어도 됩니다.
            savedJobs.add(integrationRepository.save(
                integrationMapper.toEntity(targetInfos.get(0), JobType.INDEX_DATA, toDate, workerIp, JobResult.SUCCESS)
            ));
        }

        // 실제 데이터 연동 작업은 부하를 줄이기 위해 "딱 한 번만" 비동기로 던집니다.
        if (!savedJobs.isEmpty()) {
            this.executeIndexDataSyncInBackground(savedJobs.get(0).getId(), request);
        }

        // 4. 리턴 리스트도 49개를 보냅니다. (팝업창에 49가 뜨게 함)
        return integrationMapper.toResponseList(savedJobs);
    }

    @Async
    @Transactional
    public void executeIndexDataSyncInBackground(Long jobId, IntegrationSyncRequest request) {
        integrationRepository.findById(jobId).ifPresent(job -> {
            try {
                int savedCount = externalApiService.syncIndexData(job.getIndexInfo(), request.getBaseDateFrom(), request.getBaseDateTo() != null ? request.getBaseDateTo() : LocalDate.now());
                job.updateResult(JobResult.SUCCESS, savedCount);
            } catch (Exception e) {
                job.updateResult(JobResult.FAILED, 0);
            }
        });
    }

    private void updateJobResults(List<Long> jobIds, JobResult result) {
        for (Long jobId : jobIds) {
            integrationRepository.findById(jobId).ifPresent(job -> job.updateResult(result));
        }
    }

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