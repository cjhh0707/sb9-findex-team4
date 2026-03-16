package com.sprint.findex.domain.integration.service;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
import com.sprint.findex.domain.integration.dto.response.CursorPageResponse;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import com.sprint.findex.domain.integration.mapper.IntegrationMapper;
import com.sprint.findex.domain.integration.repository.IntegrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationMapper integrationMapper;

    // 외부 API 호출을 위한 우체부 (설정 클래스에서 빈으로 등록되어 있어야 합니다)
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public CursorPageResponse<IntegrationResponse> getIntegrations(IntegrationSearchCondition condition) {

        List<Integration> integrations = integrationRepository.findIntegrationsByCondition(condition);

        List<IntegrationResponse> content = integrationMapper.toResponseList(integrations);

        boolean hasNext = false;
        Long nextIdAfter = null;

        if (!content.isEmpty()) {
            if (content.size() >= (condition.getSize() != null ? condition.getSize() : 10)) {
                hasNext = true;
            }
            nextIdAfter = content.get(content.size() - 1).getId();
        }

        return new CursorPageResponse<>(
                content,
                null,
                nextIdAfter,
                content.size(),
                null,
                hasNext
        );
    }

    @Async
    @Transactional
    public void syncIndexData(IntegrationSyncRequest request, String workerIp) {
        log.info("[비동기 연동 시작] 대상 지수 IDs: {}, 기간: {} ~ {}",
                request.getIndexInfoIds(), request.getBaseDateFrom(), request.getBaseDateTo());

        try {
            IndexInfo dummyIndexInfo = null;

            Integration successHistory = integrationMapper.toEntity(
                    dummyIndexInfo,
                    JobType.INDEX_DATA,
                    LocalDate.now(),
                    workerIp,
                    JobResult.SUCCESS
            );
            integrationRepository.save(successHistory);

            log.info("[비동기 연동 완료] 성공적으로 저장되었습니다.");

        } catch (Exception e) {
            log.error("[비동기 연동 실패] 오류 발생: {}", e.getMessage(), e);

            IndexInfo dummyIndexInfo = null;

            Integration failedHistory = integrationMapper.toEntity(
                    dummyIndexInfo,
                    JobType.INDEX_DATA,
                    LocalDate.now(),
                    workerIp,
                    JobResult.FAIL
            );
            integrationRepository.save(failedHistory);
        }
    }

    @Transactional
    public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
        log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);

        // 1. 'NEW' 상태로 작업 이력을 DB에 먼저 저장합니다.
        Integration newJob = integrationMapper.toEntity(
                null,
                JobType.INDEX_INFO,
                LocalDate.now(),
                workerIp,
                JobResult.NEW
        );
        Integration savedJob = integrationRepository.save(newJob);

        // 2. 비동기 백그라운드 작업을 호출합니다.
        this.executeOpenApiSyncInBackground(savedJob.getId());

        // 3. 저장된 단건 데이터를 List로 감싸서 DTO로 반환합니다.
        return integrationMapper.toResponseList(List.of(savedJob));
    }

    @Async
    @Transactional
    public void executeOpenApiSyncInBackground(Long jobId) {
        log.info("[백그라운드 작업 시작] 지수 정보 연동 (Job ID: {})", jobId);

        try {
            // (TODO) 실제 Open API 호출 로직이 들어갈 자리입니다.

            integrationRepository.findById(jobId).ifPresent(job -> {
                job.updateResult(JobResult.SUCCESS);
            });
            log.info("[백그라운드 작업 완료] 지수 정보 연동 성공");

        } catch (Exception e) {
            log.error("[백그라운드 작업 실패] 오류 발생: {}", e.getMessage(), e);
            integrationRepository.findById(jobId).ifPresent(job -> {
                job.updateResult(JobResult.FAIL);
            });
        }
    }
}