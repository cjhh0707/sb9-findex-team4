package com.sprint.findex.domain.autointegration.scheduler;

import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoIntegrationScheduler {

  private final AutoIntegrationRepository autoIntegrationRepository;
  private final AutoIntegrationService autoIntegrationService;

//  자동 연동 배치 작업 -> 이전 작업 끝날시 설정된 시간 뒤에 다시 실행
//  값은 yml 에서 주입 (
  @Scheduled(fixedDelayString = "${batch.sync.enabled-index-data.fixed-delay}")
  public void runAutoIntegration() {

    List<AutoIntegration> targets =
        autoIntegrationRepository.findAllByEnabled(true);

    for (AutoIntegration autoIntegration : targets) {

      Long id = autoIntegration.getId();

      autoIntegrationService.updateLastIntegrationDate(id, null);
    }
  }
}
