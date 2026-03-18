package com.sprint.findex.domain.indexdata.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexdata.dto.*;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataMapper indexDataMapper;

  @Transactional
  public IndexDataResponse save(IndexDataCreateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
        .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다."));

    IndexData savedData = indexDataRepository.save(indexDataMapper.toEntity(request, indexInfo));
    return indexDataMapper.toResponse(savedData);
  }

  public CursorPageResponse<IndexDataResponse> search(
      Long indexInfoId, LocalDate startDate, LocalDate endDate,
      Long idAfter, String sortField, String sortDirection, int size) {

    // ⭐ 1. [궁극의 해결책] 화면에 스크롤바가 생기지 않는 프론트엔드 버그를 우회하기 위해,
    // 백엔드에서 강제로 한 번에 100개씩 넉넉하게 주도록 사이즈를 덮어씌웁니다!
    size = 100;

    // 2. 방어 로직
    if (idAfter == null) idAfter = 0L;
    if (sortField == null || sortField.isBlank()) sortField = "baseDate";
    String finalSortDirection = (sortDirection == null || sortDirection.isBlank()) ? "desc" : sortDirection;

    LocalDate finalStartDate = (startDate == null) ? LocalDate.of(2000, 1, 1) : startDate;
    LocalDate finalEndDate = (endDate == null) ? LocalDate.of(2099, 12, 31) : endDate;

    // 3. DB 전체 조회
    List<IndexData> allData = indexDataRepository.findAll();

    // 4. 필터링 및 정렬 (메모리에서 초고속 처리)
    List<IndexData> filteredData = allData.stream()
        .filter(d -> indexInfoId == null || indexInfoId == 0L || d.getIndexInfo().getId().equals(indexInfoId))
        .filter(d -> d.getBaseDate().compareTo(finalStartDate) >= 0 && d.getBaseDate().compareTo(finalEndDate) <= 0)
        .sorted((d1, d2) -> {
          // ⭐ [안전장치] 날짜가 완전히 똑같은 데이터가 있을 경우, ID 순으로 정렬해서 순서가 꼬이는 걸 방지합니다.
          int dateCompare = finalSortDirection.equalsIgnoreCase("desc") ?
              d2.getBaseDate().compareTo(d1.getBaseDate()) :
              d1.getBaseDate().compareTo(d2.getBaseDate());
          return dateCompare != 0 ? dateCompare : d2.getId().compareTo(d1.getId());
        })
        .toList();

    // 5. 페이지 사이즈(100개)만큼 자르기
    java.util.List<IndexData> pagedData = new java.util.ArrayList<>();
    boolean found = (idAfter == 0L);
    boolean hasNext = false;

    for (IndexData data : filteredData) {
      if (!found) {
        if (data.getId().equals(idAfter)) found = true;
        continue;
      }
      if (pagedData.size() < size) {
        pagedData.add(data);
      } else {
        hasNext = true;
        break;
      }
    }

    // 6. 응답 DTO 변환
    List<IndexDataResponse> content = pagedData.stream()
        .map(data -> new IndexDataResponse(
            data.getId(),
            data.getIndexInfo().getId(),
            data.getBaseDate(),
            data.getSourceType(),
            data.getMarketPrice() == null ? java.math.BigDecimal.ZERO : data.getMarketPrice(),
            data.getClosingPrice() == null ? java.math.BigDecimal.ZERO : data.getClosingPrice(),
            data.getHighPrice() == null ? java.math.BigDecimal.ZERO : data.getHighPrice(),
            data.getLowPrice() == null ? java.math.BigDecimal.ZERO : data.getLowPrice(),
            data.getVersus() == null ? java.math.BigDecimal.ZERO : data.getVersus(),
            data.getFluctuationRate() == null ? java.math.BigDecimal.ZERO : data.getFluctuationRate(),
            data.getTradingQuantity() == null ? 0L : data.getTradingQuantity().longValue(),
            data.getTradingPrice() == null ? 0L : data.getTradingPrice().longValue(),
            data.getMarketTotalAmount() == null ? 0L : data.getMarketTotalAmount().longValue()
        )).toList();

    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();
    long totalCount = filteredData.size();

    return new CursorPageResponse<>(content, null, nextIdAfter, size, totalCount, hasNext);
  }
  public IndexDataResponse findById(Long id) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 데이터를 찾을 수 없습니다. ID: " + id));
    return indexDataMapper.toResponse(indexData);
  }

  @Transactional
  public IndexDataResponse update(Long id, IndexDataUpdateRequest request) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("수정할 데이터를 찾을 수 없습니다. ID: " + id));
    indexDataMapper.updateEntityFromDto(request, indexData);
    return indexDataMapper.toResponse(indexData);
  }

  @Transactional
  public void delete(Long id) {
    IndexData indexData = indexDataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("삭제할 데이터를 찾을 수 없습니다. ID: " + id));
    indexDataRepository.delete(indexData);
  }

  public String exportToCsv(Long indexInfoId, LocalDate startDate, LocalDate endDate, String sortField, String sortDirection) {
    Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

    List<IndexData> dataList = indexDataRepository.findAllForExport(indexInfoId, startDate, endDate, sort);

    StringBuilder csv = new StringBuilder();
    csv.append("기준일자,지수명,종가,대비,등락률,거래량,거래대금\n");

    for (IndexData data : dataList) {
      csv.append(data.getBaseDate()).append(",")
          .append(data.getIndexInfo().getIndexName()).append(",")
          .append(data.getClosingPrice() == null ? 0 : data.getClosingPrice()).append(",")
          .append(data.getVersus() == null ? 0 : data.getVersus()).append(",")
          .append(data.getFluctuationRate() == null ? 0 : data.getFluctuationRate()).append(",")
          .append(data.getTradingQuantity() == null ? 0 : data.getTradingQuantity()).append(",")
          .append(data.getTradingPrice() == null ? 0 : data.getTradingPrice()).append("\n");
    }
    return csv.toString();
  }
}