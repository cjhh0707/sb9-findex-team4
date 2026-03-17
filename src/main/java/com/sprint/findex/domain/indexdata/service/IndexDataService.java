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

    // 1. 파라미터 null 방어 로직
    if (idAfter == null) idAfter = 0L;
    if (indexInfoId == null) indexInfoId = 1L;

    // 2. DB 조회를 위한 날짜 처리
    LocalDate finalStartDate = (startDate == null) ? LocalDate.of(2000, 1, 1) : startDate;
    LocalDate finalEndDate = (endDate == null) ? LocalDate.of(2099, 12, 31) : endDate;

    // 3. 정렬 및 페이징 설정
    Sort sort = sortDirection.equalsIgnoreCase("desc")
        ? Sort.by(sortField).descending()
        : Sort.by(sortField).ascending();
    Pageable pageable = PageRequest.of(0, size, sort);

    // 4. DB 조회 (반드시 finalStartDate, finalEndDate 사용)
    Slice<IndexData> result = indexDataRepository.searchIndexData(indexInfoId, finalStartDate, finalEndDate, idAfter, pageable);

    // 5. [해결] BigDecimal을 Long/Double로 변환하고 null인 경우 0을 채워줍니다.
    List<IndexDataResponse> content = result.getContent().stream()
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
            // Long 타입 필드들 (null이면 0L)
            data.getTradingQuantity() == null ? 0L : data.getTradingQuantity().longValue(),
            data.getTradingPrice() == null ? 0L : data.getTradingPrice().longValue(),
            data.getMarketTotalAmount() == null ? 0L : data.getMarketTotalAmount().longValue()
        )).toList();

    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();

    return new CursorPageResponse<>(content, null, nextIdAfter, size, 0L, result.hasNext());  }

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