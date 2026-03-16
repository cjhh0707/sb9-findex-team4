package com.sprint.findex.domain.dashboard.service.basic;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexRankingResponse;
import com.sprint.findex.domain.dashboard.dto.response.IndexSummaryResponse;
import com.sprint.findex.domain.dashboard.service.DashboardService;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicDashboardService implements DashboardService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    // 즐겨찾기 지수 성과 조회
    @Override
    public List<IndexSummaryResponse> getFavoritePerformance(PeriodType periodType) {
        List<IndexInfo> favoriteList = indexInfoRepository.findAllByFavoriteTrue();
        List<Long> idList = favoriteList.stream().map(IndexInfo::getId).toList();

        LocalDate currentDate = LocalDate.now();
        LocalDate pastDate = calculateMinusDate(periodType);

        // 현재/과거 데이터 조회
        List<IndexData> currentDataList = indexDataRepository
                .findMostRecentByIndexInfoIdsAndMaxDate(idList, currentDate);
        List<IndexData> pastDataList = indexDataRepository
                .findClosestPastByIndexInfoIdsAndTargetDate(idList, pastDate, pastDate.minusDays(30));

        // Map으로 변환 (O(1) 조회)
        Map<Long, IndexData> currentMap = currentDataList.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));
        Map<Long, IndexData> pastMap = pastDataList.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));

        return favoriteList.stream()
                .map(indexInfo -> {
                    IndexData current = currentMap.get(indexInfo.getId());
                    IndexData past = pastMap.get(indexInfo.getId());

                    if (current == null || past == null) return null;

                    BigDecimal currentPrice = current.getClosingPrice();
                    BigDecimal pastPrice = past.getClosingPrice();
                    BigDecimal versus = currentPrice.subtract(pastPrice);
                    BigDecimal fluctuationRate = BigDecimal.ZERO;

                    if (pastPrice.compareTo(BigDecimal.ZERO) != 0) {
                        fluctuationRate = versus.divide(pastPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    return IndexSummaryResponse.builder()
                            .indexInfoId(indexInfo.getId())
                            .indexClassificationName(indexInfo.getIndexClassificationName())
                            .indexName(indexInfo.getIndexName())
                            .versus(versus)
                            .fluctuationRate(fluctuationRate)
                            .currentPrice(currentPrice)
                            .beforePrice(pastPrice)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 지수 차트 조회
    @Override
    public IndexChartResponse getChart(Long indexInfoId, ChartPeriodType periodType) {
        if (indexInfoId == null) return null;

        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new NoSuchElementException("지수 정보를 찾을 수 없습니다: " + indexInfoId));

        // 가장 최근 데이터 기준으로 날짜 계산
        Optional<IndexData> latestOpt = indexDataRepository
                .findTopByIndexInfoIdOrderByBaseDateDesc(indexInfoId);

        if (latestOpt.isEmpty()) return null;

        LocalDate endDate = latestOpt.get().getBaseDate();
        LocalDate startDate = switch (periodType) {
            case MONTHLY -> endDate.minusMonths(1);
            case QUARTERLY -> endDate.minusMonths(3);
            case YEARLY -> endDate.minusYears(1);
        };

        // 이동평균선 계산을 위해 startDate 30일 전 데이터도 포함
        List<IndexData> allData = indexDataRepository
                .findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
                        indexInfoId, startDate.minusDays(30), endDate);

        List<IndexChartResponse.ChartDataPoint> dataPoints = new ArrayList<>();
        List<IndexChartResponse.ChartDataPoint> ma5DataPoints = new ArrayList<>();
        List<IndexChartResponse.ChartDataPoint> ma20DataPoints = new ArrayList<>();

        // 슬라이딩 윈도우 방식으로 이동평균선 계산
        double ma5Sum = 0.0;
        double ma20Sum = 0.0;
        final int ma5Window = 5;
        final int ma20Window = 20;

        for (int i = 0; i < allData.size(); i++) {
            IndexData data = allData.get(i);
            LocalDate date = data.getBaseDate();
            double closingPrice = data.getClosingPrice().doubleValue();

            ma5Sum += closingPrice;
            ma20Sum += closingPrice;

            if (i >= ma5Window) ma5Sum -= allData.get(i - ma5Window).getClosingPrice().doubleValue();
            if (i >= ma20Window) ma20Sum -= allData.get(i - ma20Window).getClosingPrice().doubleValue();

            // startDate 이후 데이터만 응답에 포함
            if (!date.isBefore(startDate)) {
                dataPoints.add(new IndexChartResponse.ChartDataPoint(date, data.getClosingPrice()));

                ma5DataPoints.add(new IndexChartResponse.ChartDataPoint(
                        date, i >= ma5Window - 1
                        ? BigDecimal.valueOf(ma5Sum / ma5Window).setScale(2, RoundingMode.HALF_UP)
                        : null));

                ma20DataPoints.add(new IndexChartResponse.ChartDataPoint(
                        date, i >= ma20Window - 1
                        ? BigDecimal.valueOf(ma20Sum / ma20Window).setScale(2, RoundingMode.HALF_UP)
                        : null));
            }
        }

        return IndexChartResponse.builder()
                .indexInfoId(indexInfo.getId())
                .indexClassificationName(indexInfo.getIndexClassificationName())
                .indexName(indexInfo.getIndexName())
                .periodType(periodType)
                .dataPoints(dataPoints)
                .ma5DataPoints(ma5DataPoints)
                .ma20DataPoints(ma20DataPoints)
                .build();
    }

    // 성과 랭킹 조회
    @Override
    public List<IndexRankingResponse> getPerformanceRank(Long indexInfoId, PeriodType periodType, int limit) {
        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();
        List<Long> idList = allIndexInfos.stream().map(IndexInfo::getId).toList();

        LocalDate currentDate = LocalDate.now();
        LocalDate pastDate = calculateMinusDate(periodType);

        List<IndexData> currentDataList = indexDataRepository
                .findMostRecentByIndexInfoIdsAndMaxDate(idList, currentDate);
        List<IndexData> pastDataList = indexDataRepository
                .findClosestPastByIndexInfoIdsAndTargetDate(idList, pastDate, pastDate.minusDays(30));

        Comparator<IndexData> byDateThenId = Comparator
                .comparing(IndexData::getBaseDate)
                .thenComparing(IndexData::getId);

        Map<Long, IndexData> currentMap = currentDataList.stream()
                .collect(Collectors.toMap(
                        d -> d.getIndexInfo().getId(), d -> d,
                        (a, b) -> byDateThenId.compare(a, b) >= 0 ? a : b));
        Map<Long, IndexData> pastMap = pastDataList.stream()
                .collect(Collectors.toMap(
                        d -> d.getIndexInfo().getId(), d -> d,
                        (a, b) -> byDateThenId.compare(a, b) >= 0 ? a : b));

        // 성과 계산 후 등락률 기준 내림차순 정렬
        List<IndexSummaryResponse> performances = allIndexInfos.stream()
                .map(indexInfo -> {
                    IndexData current = currentMap.get(indexInfo.getId());
                    IndexData past = pastMap.get(indexInfo.getId());

                    if (current == null || past == null) return null;

                    BigDecimal currentPrice = current.getClosingPrice();
                    BigDecimal pastPrice = past.getClosingPrice();
                    BigDecimal versus = currentPrice.subtract(pastPrice);
                    BigDecimal fluctuationRate = BigDecimal.ZERO;

                    if (pastPrice.compareTo(BigDecimal.ZERO) != 0) {
                        fluctuationRate = versus.divide(pastPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    return IndexSummaryResponse.builder()
                            .indexInfoId(indexInfo.getId())
                            .indexClassificationName(indexInfo.getIndexClassificationName())
                            .indexName(indexInfo.getIndexName())
                            .versus(versus)
                            .fluctuationRate(fluctuationRate)
                            .currentPrice(currentPrice)
                            .beforePrice(pastPrice)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        IndexSummaryResponse::getFluctuationRate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // 랭킹 부여
        List<IndexRankingResponse> allRanked = IntStream.range(0, performances.size())
                .mapToObj(i -> {
                    IndexSummaryResponse p = performances.get(i);
                    return IndexRankingResponse.builder()
                            .rank(i + 1)
                            .indexInfoId(p.getIndexInfoId())
                            .indexClassificationName(p.getIndexClassificationName())
                            .indexName(p.getIndexName())
                            .versus(p.getVersus())
                            .fluctuationRate(p.getFluctuationRate())
                            .currentPrice(p.getCurrentPrice())
                            .beforePrice(p.getBeforePrice())
                            .build();
                })
                .collect(Collectors.toList());

        // indexInfoId 있으면 단일 조회
        if (indexInfoId != null) {
            return allRanked.stream()
                    .filter(r -> Objects.equals(r.getIndexInfoId(), indexInfoId))
                    .collect(Collectors.toList());
        }

        // limit 적용
        int trueLimit = Math.min(limit, allRanked.size());
        return new ArrayList<>(allRanked.subList(0, trueLimit));
    }

    // 기간 계산
    private LocalDate calculateMinusDate(PeriodType periodType) {
        LocalDate current = LocalDate.now();
        return switch (periodType) {
            case DAILY -> current.minusDays(1);
            case WEEKLY -> current.minusDays(7);
            case MONTHLY -> current.minusMonths(1);
        };
    }
}