//package com.sprint.findex.domain.integration.service;
//
//import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
//import com.sprint.findex.domain.integration.dto.openapi.OpenApiMarketIndexResponse;
//import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
//import com.sprint.findex.domain.integration.dto.request.IntegrationSyncRequest;
//import com.sprint.findex.domain.integration.dto.response.CursorPageResponse;
//import com.sprint.findex.domain.integration.dto.response.IntegrationResponse;
//import com.sprint.findex.domain.integration.entity.Integration;
//import com.sprint.findex.domain.integration.entity.JobResult;
//import com.sprint.findex.domain.integration.entity.JobType;
//import com.sprint.findex.domain.integration.mapper.IntegrationMapper;
//import com.sprint.findex.domain.integration.repository.IntegrationRepository;
//import java.net.URI;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDate;
//import java.util.List;
//import org.springframework.web.util.UriComponentsBuilder;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class IntegrationService {
//
//  private final IntegrationRepository integrationRepository;
//  private final IntegrationMapper integrationMapper;
//  private final IndexInfoRepository indexInfoRepository;
//  // 외부 API 호출을 위한 우체부 (설정 클래스에서 빈으로 등록되어 있어야 합니다)
//  private final RestTemplate restTemplate;
//
//  @Value("${external.api.baseurl}")
//  private String baseUrl;
//
//  @Value("${external.api.apiKey}")
//  private String serviceKey;
//
//  // 연동 이력 목록 조회
//  @Transactional(readOnly = true)
//  public CursorPageResponse<IntegrationResponse> getIntegrations(
//      IntegrationSearchCondition condition) {
//
//    List<Integration> integrations = integrationRepository.findIntegrationsByCondition(condition);
//
//    List<IntegrationResponse> content = integrationMapper.toResponseList(integrations);
//
//    boolean hasNext = false;
//    Long nextIdAfter = null;
//
//    if (!content.isEmpty()) {
//      if (content.size() >= (condition.getSize() != null ? condition.getSize() : 10)) {
//        hasNext = true;
//      }
//      nextIdAfter = content.get(content.size() - 1).getId();
//    }
//
//    return new CursorPageResponse<>(
//        content,
//        null,
//        nextIdAfter,
//        content.size(),
//        null,
//        hasNext
//    );
//  }
//
//  // [지수 데이터 연동] 메인 (컨트롤러에서 호출됨)
//
//  @Transactional
//  public List<IntegrationResponse> syncIndexData(IntegrationSyncRequest request, String workerIp) {
//    log.info("[지수 데이터 연동 요청 접수] 대상 지수 IDs: {}, 기간: {} ~ {}",
//        request.getIndexInfoIds(), request.getBaseDateFrom(), request.getBaseDateTo());
//
//    IndexInfo dummyIndexInfo = null;
//    Integration newJob = integrationMapper.toEntity(
//        dummyIndexInfo,
//        JobType.INDEX_DATA,
//        LocalDate.now(),
//        workerIp,
//        JobResult.NEW
//    );
//    Integration savedJob = integrationRepository.save(newJob);
//    this.executeDataSyncInBackground(savedJob.getId(), request);
//    return integrationMapper.toResponseList(List.of(savedJob));
//  }
//
//  // [지수 데이터 연동] 백그라운드 작업
//  @Async
//  @Transactional
//  public void executeDataSyncInBackground(Long jobId, IntegrationSyncRequest request) {
//    log.info("[백그라운드 작업 시작] 지수 데이터 연동 (Job ID: {})", jobId);
//    try {
//      // TODO: 실제 Open API 호출 및 데이터 저장 로직 수행
//
//      integrationRepository.findById(jobId).ifPresent(job -> {
//        job.updateResult(JobResult.SUCCESS);
//      });
//      log.info("[백그라운드 작업 완료] 지수 데이터 연동 성공");
//
//    } catch (Exception e) {
//      log.error("[백그라운드 작업 실패] 오류 발생: {}", e.getMessage(), e);
//      integrationRepository.findById(jobId).ifPresent(job -> {
//        job.updateResult(JobResult.FAIL);
//      });
//    }
//  }
//
//  // [지수 정보 연동] 메인
//  @Transactional
//  public List<IntegrationResponse> createIndexInfoSyncJob(String workerIp) {
//    if (workerIp == null || workerIp.trim().isEmpty()) {
//      throw new IllegalArgumentException("작업자 IP가 누락되었습니다. 다시 확인해주세요.");
//    }
//    log.info("[지수 정보 연동 요청 접수] 작업자 IP: {}", workerIp);
//
//    Integration newJob = integrationMapper.toEntity(
//        null,
//        JobType.INDEX_INFO,
//        LocalDate.now(),
//        workerIp,
//        JobResult.NEW
//    );
//    Integration savedJob = integrationRepository.save(newJob);
//    this.executeOpenApiSyncInBackground(savedJob.getId());
//    return integrationMapper.toResponseList(List.of(savedJob));
//  }
//
//  // [지수 정보 연동] 백그라운드 작업
//  @Async
//  @Transactional
//  public void executeOpenApiSyncInBackground(Long jobId) {
//    log.info("[백그라운드 작업 시작] 지수 정보 연동 (Job ID: {})", jobId);
//
//    try {
//      // URI 생성 로직 (생략되었던 부분)
//      URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/getStockMarketIndex")
//          .queryParam("serviceKey", serviceKey)
//          .queryParam("resultType", "json")
//          .queryParam("numOfRows", "100")
//          .queryParam("pageNo", "1")
//          .build(true)
//          .toUri();
//
//      log.info("호출 URI: {}", uri);
//
//      // 외부 API 호출
//      OpenApiMarketIndexResponse apiResponse = restTemplate.getForObject(uri,
//          OpenApiMarketIndexResponse.class);
//
//      // 응답 데이터 검증 및 저장
//      if (apiResponse != null && apiResponse.getResponse() != null
//          && "00".equals(apiResponse.getResponse().getHeader().getResultCode())) {
//
//        List<OpenApiMarketIndexResponse.Item> apiItems = apiResponse.getResponse().getBody()
//            .getItems().getItem();
//        log.info("성공적으로 {}개의 데이터를 가져왔습니다.", apiItems.size());
//
//        for (OpenApiMarketIndexResponse.Item item : apiItems) {
//          IndexInfo indexInfo = IndexInfo.builder()
//              .indexClassificationName(item.getIdxCsf()) // 지수분류명
//              .indexName(item.getIdxNm())               // 지수명
//              .baseIndex(Double.parseDouble(item.getClpr())) // 종가(기준지수)
//              .sourceType("OPEN_API")
//              .favorite(false)
//              .build();
//
//          indexInfoRepository.save(indexInfo);
//        }
//
//        // 작업 이력 상태를 SUCCESS로 업데이트
//        integrationRepository.findById(jobId).ifPresent(job -> {
//          job.updateResult(JobResult.SUCCESS);
//        });
//
//        log.info("[백그라운드 작업 완료] Job ID: {} 성공적으로 종료", jobId);
//
//      } else {
//        throw new RuntimeException("API 응답 결과가 정상이 아닙니다. (resultCode 확인 필요)");
//      }
//
//    } catch (Exception e) {
//      log.error("[백그라운드 작업 실패] Job ID: {}, 오류 발생: {}", jobId, e.getMessage(), e);
//
//      // 실패 시 작업 이력 상태를 FAIL로 업데이트
//      integrationRepository.findById(jobId).ifPresent(job -> {
//        job.updateResult(JobResult.FAIL);
//      });
//    }
//  }
//}