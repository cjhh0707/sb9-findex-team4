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

        return new CursorPageResponse<>(
                content,
                nextIdAfter != null ? String.valueOf(nextIdAfter) : null,
                nextIdAfter,
                size,
                totalElements,
                hasNext
        );
    }

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        JobResult result = JobResult.FAILED;
        try {
            externalApiService.syncIndexInfo();
            result = JobResult.SUCCESS;
        } catch (Exception e) {
            log.error("[지수 정보 연동 실패] error: {}", e.getMessage());
        }

        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();
        JobResult finalResult = result;
        List<Integration> savedJobs = allIndexInfos.stream().map(indexInfo ->
                integrationRepository.save(
                        integrationMapper.toEntity(indexInfo, JobType.INDEX_INFO,
                                LocalDate.now(), workerIp, finalResult)
                )
        ).toList();

        log.info("[지수 정보 연동 완료] 처리 건수: {}, 결과: {}", savedJobs.size(), finalResult);
        return integrationMapper.toResponseList(savedJobs);
    }

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
                List<LocalDate> syncedDates = externalApiService.syncIndexData(indexInfo, fromDate, toDate);

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