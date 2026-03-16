package com.sprint.findex.domain.dashboard.service.basic;

import com.sprint.findex.domain.dashboard.dto.ChartPeriodType;
import com.sprint.findex.domain.dashboard.dto.PeriodType;
import com.sprint.findex.domain.dashboard.dto.response.ChartDataPoint;
import com.sprint.findex.domain.dashboard.dto.response.IndexChartDto;
import com.sprint.findex.domain.dashboard.dto.response.IndexPerformanceDto;
import com.sprint.findex.domain.dashboard.dto.response.RankedIndexPerformanceDto;
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

    @Override
    public List<IndexPerformanceDto> getFavoritePerformance(PeriodType periodType) {
        List<IndexInfo> favoriteList = indexInfoRepository.findAllByFavoriteTrue();
        if(favoriteList.isEmpty()) return List.of();

        List<Long> idList = favoriteList.stream().map(IndexInfo::getId).toList();

        LocalDate currentDate = LocalDate.now();
        LocalDate pastDate = calculateMinusDate(periodType);

        List<IndexData> currentDataList = indexDataRepository.findMostRecentByIndexInfoIdsAndMaxDate(idList, currentDate);
        List<IndexData> pastDataList = indexDataRepository.findClosestPastByIndexInfoIdsAndTargetDate(idList, pastDate, pastDate.minusDays(30));

        Map<Long, IndexData> currentMap = currentDataList.stream().collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));
        Map<Long, IndexData> pastMap = pastDataList.stream().collect(Collectors.toMap(d -> d.getIndexInfo().getId(), d -> d));

        return favoriteList.stream().map(indexInfo -> {
            IndexData current = currentMap.get(indexInfo.getId());
            IndexData past = pastMap.get(indexInfo.getId());

            if (current == null || past == null) return null;

            BigDecimal currentPrice = current.getClosingPrice();
            BigDecimal pastPrice = past.getClosingPrice();
            BigDecimal versus = currentPrice.subtract(pastPrice);
            BigDecimal fluctuationRate = BigDecimal.ZERO;

            if (pastPrice.compareTo(BigDecimal.ZERO) != 0) {
                fluctuationRate = versus.divide(pastPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            }

            return new IndexPerformanceDto(
                    indexInfo.getId(),
                    indexInfo.getIndexClassificationName(),
                    indexInfo.getIndexName(),
                    versus,
                    fluctuationRate,
                    currentPrice,
                    pastPrice
            );
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public IndexChartDto getChart(Long indexInfoId, ChartPeriodType periodType) {
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new NoSuchElementException("지수 정보를 찾을 수 없습니다."));

        Optional<IndexData> latestOpt = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(indexInfoId);
        if (latestOpt.isEmpty()) return null;

        LocalDate endDate = latestOpt.get().getBaseDate();
        LocalDate startDate = switch (periodType) {
            case MONTHLY -> endDate.minusMonths(1);
            case QUARTERLY -> endDate.minusMonths(3);
            case YEARLY -> endDate.minusYears(1);
        };

        List<IndexData> allData = indexDataRepository.findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
                indexInfoId, startDate.minusDays(30), endDate);

        List<ChartDataPoint> dataPoints = new ArrayList<>();
        List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
        List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

        double ma5Sum = 0.0;
        double ma20Sum = 0.0;

        for (int i = 0; i < allData.size(); i++) {
            IndexData data = allData.get(i);
            LocalDate date = data.getBaseDate();
            double closingPrice = data.getClosingPrice().doubleValue();

            ma5Sum += closingPrice;
            ma20Sum += closingPrice;

            if (i >= 5) ma5Sum -= allData.get(i - 5).getClosingPrice().doubleValue();
            if (i >= 20) ma20Sum -= allData.get(i - 20).getClosingPrice().doubleValue();

            if (!date.isBefore(startDate)) {
                dataPoints.add(new ChartDataPoint(date, data.getClosingPrice()));

                ma5DataPoints.add(new ChartDataPoint(date, i >= 4
                        ? BigDecimal.valueOf(ma5Sum / 5).setScale(2, RoundingMode.HALF_UP) : null));

                ma20DataPoints.add(new ChartDataPoint(date, i >= 19
                        ? BigDecimal.valueOf(ma20Sum / 20).setScale(2, RoundingMode.HALF_UP) : null));
            }
        }

        return new IndexChartDto(
                indexInfo.getId(),
                indexInfo.getIndexClassificationName(),
                indexInfo.getIndexName(),
                periodType,
                dataPoints,
                ma5DataPoints,
                ma20DataPoints
        );
    }

    @Override
    public List<RankedIndexPerformanceDto> getPerformanceRank(Long indexInfoId, PeriodType periodType, int limit) {
        List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();
        List<Long> idList = allIndexInfos.stream().map(IndexInfo::getId).toList();

        LocalDate currentDate = LocalDate.now();
        LocalDate pastDate = calculateMinusDate(periodType);

        List<IndexData> currentDataList = indexDataRepository.findMostRecentByIndexInfoIdsAndMaxDate(idList, currentDate);
        List<IndexData> pastDataList = indexDataRepository.findClosestPastByIndexInfoIdsAndTargetDate(idList, pastDate, pastDate.minusDays(30));

        Comparator<IndexData> byDateDesc = Comparator.comparing(IndexData::getBaseDate).reversed();

        Map<Long, IndexData> currentMap = currentDataList.stream().collect(Collectors.toMap(
                d -> d.getIndexInfo().getId(), d -> d, (a, b) -> byDateDesc.compare(a, b) <= 0 ? a : b));
        Map<Long, IndexData> pastMap = pastDataList.stream().collect(Collectors.toMap(
                d -> d.getIndexInfo().getId(), d -> d, (a, b) -> byDateDesc.compare(a, b) <= 0 ? a : b));

        List<IndexPerformanceDto> performances = allIndexInfos.stream().map(indexInfo -> {
                    IndexData current = currentMap.get(indexInfo.getId());
                    IndexData past = pastMap.get(indexInfo.getId());

                    if (current == null || past == null) return null;

                    BigDecimal currentPrice = current.getClosingPrice();
                    BigDecimal pastPrice = past.getClosingPrice();
                    BigDecimal versus = currentPrice.subtract(pastPrice);
                    BigDecimal fluctuationRate = BigDecimal.ZERO;

                    if (pastPrice.compareTo(BigDecimal.ZERO) != 0) {
                        fluctuationRate = versus.divide(pastPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                    }

                    return new IndexPerformanceDto(
                            indexInfo.getId(),
                            indexInfo.getIndexClassificationName(),
                            indexInfo.getIndexName(),
                            versus,
                            fluctuationRate,
                            currentPrice,
                            pastPrice
                    );
                }).filter(Objects::nonNull)
                .sorted(Comparator.comparing(IndexPerformanceDto::fluctuationRate).reversed())
                .toList();

        List<RankedIndexPerformanceDto> allRanked = IntStream.range(0, performances.size())
                .mapToObj(i -> new RankedIndexPerformanceDto(performances.get(i), i + 1))
                .toList();

        if (indexInfoId != null) {
            return allRanked.stream().filter(r -> r.performance().indexInfoId().equals(indexInfoId)).toList();
        }

        return allRanked.stream().limit(limit).toList();
    }

    private LocalDate calculateMinusDate(PeriodType periodType) {
        LocalDate current = LocalDate.now();
        return switch (periodType) {
            case DAILY -> current.minusDays(1);
            case WEEKLY -> current.minusDays(7);
            case MONTHLY -> current.minusMonths(1);
        };
    }
}