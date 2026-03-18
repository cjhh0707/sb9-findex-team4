package com.sprint.findex.domain.integration.mapper;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
import com.sprint.findex.domain.integration.entity.Integration;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IntegrationMapper {

    public IntegrationResponse toResponse(Integration integration) {
        if (integration == null) {
            return null;
        }

        return IntegrationResponse.builder()
                .id(integration.getId())
                .indexInfoId(integration.getIndexInfo() != null ? integration.getIndexInfo().getId() : null)
                .jobType(integration.getJobType())
                .targetDate(integration.getTargetDate())
                .worker(integration.getWorker())
                .jobTime(integration.getJobTime())
                .result(integration.getResult())
                .processedCount(integration.getProcessedCount())   //⭐
                .build();
    }

    public List<IntegrationResponse> toResponseList(List<Integration> integrations) {
        if (integrations == null || integrations.isEmpty()) {
            return List.of();
        }
        return integrations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Integration toEntity(IndexInfo indexInfo, JobType jobType, LocalDate targetDate, String worker, JobResult result) {
        return Integration.builder()
                .indexInfo(indexInfo)
                .jobType(jobType)
                .targetDate(targetDate)
                .worker(worker)
                .jobTime(LocalDateTime.now())
                .result(result)
                .processedCount(0) // ⭐처음 생성할 때는 0으로 시작합니다.
                .build();
    }
}