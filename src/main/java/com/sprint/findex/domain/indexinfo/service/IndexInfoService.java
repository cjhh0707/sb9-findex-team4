package com.sprint.findex.domain.indexinfo.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.common.type.SourceType;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import com.sprint.findex.domain.indexinfo.dto.*;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final AutoIntegrationService autoIntegrationService;

  @Transactional
  public IndexInfoResponse createIndexInfo(IndexInfoCreateRequest request) {
    //중복 검증
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
        request.indexClassification(), request.indexName())) {
      throw new IllegalArgumentException("이미 등록된 지수 분류명과 지수명 조합입니다.");
    }

    //DTO -> Entity
    IndexInfo indexInfo = IndexInfo.builder()
        .indexClassification(request.indexClassification())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .sourceType(SourceType.USER)
        .favorite(request.favorite() != null ? request.favorite() : false)
        .build();

    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    // 요구사항의 "자동 연동 설정 정보도 같이 초기화되어야 합니다." 처리를 위해
    // 추후 AutoIntegrationRepository를 주입받아 비활성화 상태로 저장하는 로직 추가 필요
    autoIntegrationService.createAutoIntegration(savedIndexInfo.getId());

    return IndexInfoResponse.from(savedIndexInfo);
  }

  @Transactional
  public IndexInfoResponse getIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));
    return IndexInfoResponse.from(indexInfo);
  }

  @Transactional
  public IndexInfoResponse updateIndexInfo(Long id, IndexInfoUpdateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));

    indexInfo.updateInfo(
        request.employedItemsCount(),
        request.basePointInTime(),
        request.baseIndex()
    );
    indexInfo.updateFavorite(request.favorite());

    return IndexInfoResponse.from(indexInfo);
  }

  @Transactional
  public void deleteIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));
    indexInfoRepository.delete(indexInfo);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<IndexInfoResponse> getIndexInfoList(IndexInfoSearchCondition condition, Long idAfter, int size) {
    String sortField = condition.sortField() != null ? condition.sortField() : "indexClassification";
    String sortDirection = condition.sortDirection() != null ? condition.sortDirection() : "asc";

    Sort sort = sortDirection.equalsIgnoreCase("desc")
        ? Sort.by(sortField).descending()
        : Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(0, size + 1, sort);

    List<IndexInfo> indexInfos = indexInfoRepository.searchIndexInfos(
        condition.indexClassification(),
        condition.indexName(),
        condition.favorite(),
        idAfter,
        pageable
    );

    boolean hasNext = indexInfos.size() > size;

    List<IndexInfoResponse> content = indexInfos.stream()
        .limit(size)
        .map(IndexInfoResponse::from)
        .toList();

    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();

    long totalElements = indexInfoRepository.countIndexInfos(
        condition.indexClassification(),
        condition.indexName(),
        condition.favorite()
    );

    return new CursorPageResponse<>(
        content,
        null,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  @Transactional(readOnly = true)
  public List<IndexInfoSummaryDto> getIndexInfoSummaries() {
    return indexInfoRepository.findAll().stream()
        .map(info -> new IndexInfoSummaryDto(
            info.getId(),
            info.getIndexClassification(),
            info.getIndexName()
        ))
        .toList();
  }

}