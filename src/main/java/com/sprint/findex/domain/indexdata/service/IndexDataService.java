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

    // 정렬은 repository 쿼리의 ORDER BY i.id DESC 사용 (ID 기반 커서 페이징과 일치)
    Pageable pageable = PageRequest.of(0, size);

    Slice<IndexData> result = indexDataRepository.searchIndexData(indexInfoId, startDate, endDate, idAfter, pageable);

    List<IndexDataResponse> content = result.getContent().stream()
            .map(indexDataMapper::toResponse).toList();

    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();

    return new CursorPageResponse<>(content, null, nextIdAfter, size, null, result.hasNext());
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

    // Pageable을 활용하되, Export이므로 아주 큰 값을 넣어줍니다.
    List<IndexData> dataList = indexDataRepository.findAllForExport(indexInfoId, startDate, endDate, sort);

    StringBuilder csv = new StringBuilder();
    csv.append("기준일자,지수명,종가,대비,등락률,거래량,거래대금\n");

    for (IndexData data : dataList) {
      csv.append(data.getBaseDate()).append(",")
              .append(data.getIndexInfo().getIndexName()).append(",")
              .append(data.getClosingPrice()).append(",")
              .append(data.getVersus()).append(",")
              .append(data.getFluctuationRate()).append(",")
              .append(data.getTradingQuantity()).append(",")
              .append(data.getTradingPrice()).append("\n");
    }
    return csv.toString();
  }
}