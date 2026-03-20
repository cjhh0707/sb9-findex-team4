package com.sprint.findex.domain.openapi;

import com.sprint.findex.common.type.SourceType;
import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import com.sprint.findex.domain.openapi.dto.OpenApiResponse;
import com.sprint.findex.domain.openapi.dto.OpenApiItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 금융위원회 주가지수 OpenAPI 연동 서비스
@Slf4j
@Service
public class ExternalApiService {

    private static final String STOCK_INDEX_ENDPOINT = "/getStockMarketIndex";
    private static final int NUM_OF_ROWS = 1000;
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final AutoIntegrationRepository autoIntegrationRepository;

    public ExternalApiService(
            RestClient.Builder restClientBuilder,
            @Value("${external.api.apiKey}") String apiKey,
            @Value("${external.api.baseurl}") String baseUrl,
            IndexInfoRepository indexInfoRepository,
            IndexDataRepository indexDataRepository,
            AutoIntegrationRepository autoIntegrationRepository
    ) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.indexInfoRepository = indexInfoRepository;
        this.indexDataRepository = indexDataRepository;
        this.autoIntegrationRepository = autoIntegrationRepository;
    }

    // 지수 정보 연동: 최근 5일 데이터 기준으로 IndexInfo upsert
    @Transactional
    public int syncIndexInfo() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(5);

        List<OpenApiItem> items = fetchAllItems(null, null, startDate, endDate);
        if (items.isEmpty()) {
            log.warn("[지수 정보 연동] API에서 데이터를 가져오지 못했습니다.");
            return 0;
        }

        // (분류명|지수명) 기준 중복 제거
        Map<String, OpenApiItem> uniqueMap = new LinkedHashMap<>();
        for (OpenApiItem item : items) {
            if (item.getIdxCsf() == null || item.getIdxNm() == null) continue;
            uniqueMap.put(item.getIdxCsf() + "|" + item.getIdxNm(), item);
        }

        Map<String, IndexInfo> existingInfoMap = indexInfoRepository.findAll().stream()
                .collect(Collectors.toMap(
                        i -> i.getIndexClassification() + "|" + i.getIndexName(),
                        i -> i
                ));

        Set<Long> existingAutoIds = autoIntegrationRepository.findAll().stream()
                .map(a -> a.getIndexInfo().getId())
                .collect(Collectors.toSet());

        List<IndexInfo> toSave = new ArrayList<>();

        for (OpenApiItem item : uniqueMap.values()) {
            String key = item.getIdxCsf() + "|" + item.getIdxNm();
            IndexInfo existing = existingInfoMap.get(key);
            if (existing != null) {
                existing.updateInfo(
                        parseInteger(item.getEpyItmsCnt()),
                        parseDateDotOrPlain(item.getBasPnt()),
                        parseBigDecimal(item.getBasIdx())
                );
                toSave.add(existing);
            } else {
                toSave.add(IndexInfo.builder()
                        .indexClassification(item.getIdxCsf())
                        .indexName(item.getIdxNm())
                        .employedItemsCount(parseInteger(item.getEpyItmsCnt()))
                        .basePointInTime(parseDateDotOrPlain(item.getBasPnt()))
                        .baseIndex(parseBigDecimal(item.getBasIdx()))
                        .sourceType(SourceType.OPEN_API)
                        .favorite(false)
                        .build());
            }
        }

        List<IndexInfo> savedInfos = indexInfoRepository.saveAll(toSave);

        List<AutoIntegration> autoToSave = savedInfos.stream()
                .filter(info -> info.getId() != null && !existingAutoIds.contains(info.getId()))
                .map(info -> AutoIntegration.builder()
                        .indexInfo(info)
                        .enabled(false)
                        .build())
                .collect(Collectors.toList());

        if (!autoToSave.isEmpty()) {
            autoIntegrationRepository.saveAll(autoToSave);
        }

        log.info("[지수 정보 연동 완료] 처리 지수 수: {}", uniqueMap.size());
        return uniqueMap.size();
    }

    // 지수 데이터 연동: 특정 지수/기간의 IndexData upsert
    @Transactional
    public List<LocalDate> syncIndexData(IndexInfo indexInfo, LocalDate from, LocalDate to) {
        List<OpenApiItem> items = fetchAllItems(
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName(),
                from, to
        );

        Map<LocalDate, IndexData> existingDataMap = indexDataRepository
                .findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(indexInfo.getId(), from, to)
                .stream()
                .collect(Collectors.toMap(IndexData::getBaseDate, d -> d));

        List<IndexData> toSave = new ArrayList<>();
        List<LocalDate> syncedDates = new ArrayList<>();

        for (OpenApiItem item : items) {
            if (item.getIdxNm() == null || !item.getIdxNm().equals(indexInfo.getIndexName())
                    || item.getIdxCsf() == null || !item.getIdxCsf().equals(indexInfo.getIndexClassification())) {
                continue;
            }

            LocalDate baseDate = parseDatePlain(item.getBasDt());
            if (baseDate == null) continue;

            BigDecimal marketPrice     = parseBigDecimal(item.getMkp());
            BigDecimal closingPrice    = parseBigDecimal(item.getClpr());
            BigDecimal highPrice       = parseBigDecimal(item.getHipr());
            BigDecimal lowPrice        = parseBigDecimal(item.getLopr());
            BigDecimal versus          = parseBigDecimal(item.getVs());
            BigDecimal fluctuationRate = parseBigDecimal(item.getFltRt());
            Long tradingQuantity       = parseLong(item.getTrqu());
            Long tradingPrice          = parseLong(item.getTrPrc());
            Long marketTotalAmount     = parseLong(item.getLstgMrktTotAmt());

            IndexData existing = existingDataMap.get(baseDate);
            if (existing != null) {
                existing.update(marketPrice, closingPrice, highPrice, lowPrice,
                        versus, fluctuationRate, tradingQuantity, tradingPrice, marketTotalAmount);
                toSave.add(existing);
            } else {
                toSave.add(IndexData.builder()
                        .indexInfo(indexInfo)
                        .baseDate(baseDate)
                        .sourceType(SourceType.OPEN_API)
                        .marketPrice(marketPrice)
                        .closingPrice(closingPrice)
                        .highPrice(highPrice)
                        .lowPrice(lowPrice)
                        .versus(versus)
                        .fluctuationRate(fluctuationRate)
                        .tradingQuantity(tradingQuantity)
                        .tradingPrice(tradingPrice)
                        .marketTotalAmount(marketTotalAmount)
                        .build());
            }
            syncedDates.add(baseDate);
        }

        indexDataRepository.saveAll(toSave);

        log.info("[지수 데이터 연동 완료] 지수: {}, 기간: {} ~ {}, 처리 건수: {}",
                indexInfo.getIndexName(), from, to, syncedDates.size());
        return syncedDates;
    }

    // Private: API 호출 및 페이징
    private List<OpenApiItem> fetchAllItems(String idxCsf, String idxNm,
                                            LocalDate from, LocalDate to) {
        List<OpenApiItem> allItems = new ArrayList<>();
        int pageNo = 1;
        int totalCount = -1;

        do {
            try {
                OpenApiResponse response = callApi(idxCsf, idxNm, from, to, pageNo);
                if (response == null
                        || response.getResponse() == null
                        || response.getResponse().getBody() == null) {
                    log.warn("[OpenAPI] 빈 응답. page={}", pageNo);
                    break;
                }

                OpenApiResponse.Body body = response.getResponse().getBody();

                if (totalCount == -1) {
                    totalCount = body.getTotalCount();
                    log.debug("[OpenAPI] 총 건수: {}", totalCount);
                }

                OpenApiResponse.Items items = body.getItems();
                if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                    break;
                }

                allItems.addAll(items.getItem());
                pageNo++;

            } catch (Exception e) {
                log.error("[OpenAPI] 호출 실패. page={}, error={}", pageNo, e.getMessage(), e);
                break;
            }
        } while (allItems.size() < totalCount && totalCount > 0);

        return allItems;
    }

    private OpenApiResponse callApi(String idxCsf, String idxNm,
                                    LocalDate from, LocalDate to, int pageNo) {

        // 1. StringBuilder 준비
        StringBuilder urlBuilder = new StringBuilder(baseUrl + STOCK_INDEX_ENDPOINT);

        String encodedServiceKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        urlBuilder.append("?serviceKey=").append(encodedServiceKey);
        urlBuilder.append("&resultType=json");
        urlBuilder.append("&numOfRows=").append(NUM_OF_ROWS);
        urlBuilder.append("&pageNo=").append(pageNo);
        urlBuilder.append("&beginBasDt=").append(from.format(YYYYMMDD));
        urlBuilder.append("&endBasDt=").append(to.format(YYYYMMDD));
        if (idxCsf != null && !idxCsf.isBlank()) {
            urlBuilder.append("&idxCsf=").append(URLEncoder.encode(idxCsf, StandardCharsets.UTF_8));
        }
        if (idxNm != null && !idxNm.isBlank()) {
            urlBuilder.append("&idxNm=").append(URLEncoder.encode(idxNm, StandardCharsets.UTF_8));
        }

        URI uri = URI.create(urlBuilder.toString());
        log.debug("[OpenAPI] 인코딩된 키로 조립한 URI: {}", uri);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(OpenApiResponse.class);
    }

    // Private: 파싱 헬퍼
    /** YYYYMMDD 형식 날짜 파싱 (기준일자 basDt) */
    private LocalDate parseDatePlain(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim(), YYYYMMDD);
        } catch (Exception e) {
            log.warn("[파싱 오류] YYYYMMDD 날짜: {}", value);
            return null;
        }
    }

    /** YYYY.MM.DD 또는 YYYYMMDD 형식 날짜 파싱 (기준시점 basPnt) */
    private LocalDate parseDateDotOrPlain(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim().replace(".", ""), YYYYMMDD);
        } catch (Exception e) {
            log.warn("[파싱 오류] 날짜(dot/plain): {}", value);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
