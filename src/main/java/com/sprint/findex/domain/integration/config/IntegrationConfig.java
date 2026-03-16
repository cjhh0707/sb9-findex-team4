package com.sprint.findex.domain.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 비동기 작업(@Async)을 가능하게 해줌
@Configuration
public class IntegrationConfig {
    // RestTemplate Bean 등록 코드는 RestClient를 사용할 것이므로 삭제
}