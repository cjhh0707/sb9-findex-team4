package com.sprint.findex.domain.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@Configuration
public class IntegrationConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}