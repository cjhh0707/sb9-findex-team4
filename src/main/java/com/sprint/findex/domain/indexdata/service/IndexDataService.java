package com.sprint.findex.domain.indexdata.service;

import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataListResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataSearchCondition;
import com.sprint.findex.domain.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataMapper indexDataMapper;

  /**
   * 지수 데이터 저장 (Create)
   */
  @Transactional
  public IndexDataResponse save(IndexDataCreateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다."));

    IndexData indexData = indexDataMapper.toEntity(request, indexInfo);
    IndexData savedData = indexDataRepository.save(indexData);

    return indexDataMapper.toResponse(savedData);
  }

  /**
   * 지수 데이터 목록 조회 및 검색 (Read - 목록)
   * 필터링: 지수(ID 일치), 날짜(범위)
   * 페이징: No-offset (lastId 활용)
   */
  public Slice<IndexDataListResponse> search(IndexDataSearchCondition condition, Pageable pageable) {
    Slice<IndexData> result = indexDataRepository.searchIndexData(
            condition.indexInfoId(),
            condition.startDate(),
            condition.endDate(),
            condition.lastId(),
            pageable
    );

    return result.map(indexDataMapper::toListResponse);
  }

  /**
   * 지수 데이터 상세 조회 (Read - 단건)
   */
  public IndexDataResponse findById(Long id) {
    IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 데이터를 찾을 수 없습니다. ID: " + id));

    return indexDataMapper.toResponse(indexData);
  }

  /**
   * 지수 데이터 전체 목록 조회 (Read - 목록)
   */
  public List<IndexDataListResponse> findAll() {
    return indexDataRepository.findAll().stream()
            .map(indexDataMapper::toListResponse)
            .collect(Collectors.toList());
  }

  /**
   * [수정] 지수 데이터 정보 변경
   * 기획서 사양: 지수 정보와 날짜를 제외한 모든 속성 수정 가능
   */
  @Transactional
  public IndexDataResponse update(Long id, IndexDataUpdateRequest request) {
    IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("수정할 데이터를 찾을 수 없습니다. ID: " + id));

    // 사용자가 보낸 새 정보(DTO)를 기존 데이터(Entity)에 덮어씌움
    indexDataMapper.updateEntityFromDto(request, indexData);

    return indexDataMapper.toResponse(indexData);
  }

  /**
   * [삭제] 지수 데이터 삭제
   */
  @Transactional
  public void delete(Long id) {
    IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 데이터를 찾을 수 없습니다. ID: " + id));

    indexDataRepository.delete(indexData);
  }

  /**
   * [Export] 지수 데이터 CSV 파일 생성
   * 기획서 사양: 목록 조회와 동일한 필터링 규칙 적용, 페이지네이션 제외
   */
  public String exportToCsv(IndexDataSearchCondition condition) {
    // 레포지토리에 새로 만든 메서드로 전체 데이터 조회 (lastId와 Pageable 제외)
    List<IndexData> dataList = indexDataRepository.findAllForExport(
            condition.indexInfoId(),
            condition.startDate(),
            condition.endDate()
    );

    // CSV 헤더 작성 (기획서에 명시된 주요 속성들)
    StringBuilder csv = new StringBuilder();
    csv.append("기준일자,지수명,종가,대비,등락률,거래량,거래대금\n");

    for (IndexData data : dataList) {
      csv.append(data.getBaseDate()).append(",")
              .append(data.getIndexInfo().getIndexName()).append(",")
              .append(data.getClosingPrice()).append(",")        // 종가
              .append(data.getVersus()).append(",")             // 대비
              .append(data.getFluctuationRate()).append(",")     // 등락률
              .append(data.getTradingQuantity()).append(",")     // 거래량
              .append(data.getTradingPrice()).append("\n");      // 거래대금
    }

    return csv.toString();
  }
}