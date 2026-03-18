package com.sprint.findex.domain.autointegration.scheduler;

import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import com.sprint.findex.domain.integration.entity.JobResult;
import com.sprint.findex.domain.integration.entity.JobType;
import com.sprint.findex.domain.integration.repository.IntegrationRepository;
import com.sprint.findex.domain.integration.service.IntegrationService;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoIntegrationScheduler {

    private final AutoIntegrationRepository autoIntegrationRepository;
    private final AutoIntegrationService autoIntegrationService;
    private final IntegrationService integrationService;
    private final IntegrationRepository integrationRepository;

    //  자동 연동 배치 작업 -> 이전 작업 끝날시 설정된 시간 뒤에 다시 실행
    //  값은 yml 에서 주입
    @Scheduled(fixedDelayString = "${batch.sync.enabled-index-data.fixed-delay}")
    public void runAutoIntegration() {
        List<AutoIntegration> targets =
                autoIntegrationRepository.findAllByEnabled(true);

        LocalDate today = LocalDate.now();
        log.info("[자동 연동 스케줄러] 활성화된 연동 대상 수: {}", targets.size());

        for (AutoIntegration autoIntegration : targets) {
            IndexInfo indexInfo = autoIntegration.getIndexInfo();
            Long indexInfoId = indexInfo.getId();

            // 마지막 성공 연동일 조회
            LocalDate lastSuccessDate = integrationRepository
                    .findLastSuccessTargetDate(indexInfoId, JobType.INDEX_DATA, JobResult.SUCCESS)
                    .orElse(null);

            // fromDate: 마지막 성공일 다음날, 없으면 오늘
            LocalDate fromDate = (lastSuccessDate != null)
                    ? lastSuccessDate.plusDays(1)
                    : today;

            // 이미 오늘까지 연동됐거나 미래면 스킵
            if (fromDate.isAfter(today)) {
                log.info("[자동 연동 스킵] 지수: {} - 이미 최신 상태 (마지막 성공일: {})",
                        indexInfo.getIndexName(), lastSuccessDate);
                continue;
            }

            log.info("[자동 연동 시작] 지수: {}, 기간: {} ~ {}",
                    indexInfo.getIndexName(), fromDate, today);

            try {
                integrationService.runBatchSync(indexInfo, fromDate, today, "system");
                autoIntegrationService.updateLastIntegrationDate(autoIntegration.getId(), LocalDateTime.now());
                log.info("[자동 연동 완료] 지수: {}", indexInfo.getIndexName());
            } catch (Exception e) {
                log.error("[자동 연동 실패] 지수: {}, error: {}", indexInfo.getIndexName(), e.getMessage(), e);
            }
        }
    }
}
