package com.sprint.findex.domain.indexinfo.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import com.sprint.findex.domain.indexinfo.dto.*;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.mapper.IndexInfoMapper;
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
  private final IndexInfoMapper indexInfoMapper;

  @Transactional
  public IndexInfoResponse createIndexInfo(IndexInfoCreateRequest request) {
    // 1. 중복 검증
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
            request.indexClassification(), request.indexName())) {
      throw new IllegalArgumentException("이미 등록된 지수 분류명과 지수명 조합입니다.");
    }

    // 2. Entity 변환 및 저장
    IndexInfo indexInfo = indexInfoMapper.toEntity(request);
    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    // 3. 자동 연동 설정 정보 초기화
    autoIntegrationService.createAutoIntegration(savedIndexInfo.getId());

    // 4. Entity -> Response DTO 반환
    return indexInfoMapper.toResponse(savedIndexInfo);
  }

  @Transactional(readOnly = true)
  public IndexInfoResponse getIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));
    return indexInfoMapper.toResponse(indexInfo);
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

    return indexInfoMapper.toResponse(indexInfo);
  }

  @Transactional
  public void deleteIndexInfo(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));
    indexInfoRepository.delete(indexInfo);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<IndexInfoResponse> getIndexInfoList(IndexInfoSearchCondition condition, Long idAfter, String cursor, int size) {

    // cursor를 페이지 번호로 해석 (0부터 시작)
    int pageNum = 0;
    if (cursor != null && !cursor.isBlank()) {
      try {
        pageNum = Integer.parseInt(cursor);
      } catch (NumberFormatException e) {
        pageNum = 0;
      }
    }

    String sortField = condition.sortField() != null ? condition.sortField() : "indexClassification";
    String sortDirection = condition.sortDirection() != null ? condition.sortDirection() : "asc";

    Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

    long totalElements = indexInfoRepository.countIndexInfos(
            condition.indexClassification(),
            condition.indexName(),
            condition.favorite()
    );

    Pageable pageable = PageRequest.of(pageNum, size, sort);

    List<IndexInfo> indexInfos = indexInfoRepository.searchIndexInfos(
            condition.indexClassification(),
            condition.indexName(),
            condition.favorite(),
            pageable
    );

    List<IndexInfoResponse> content = indexInfos.stream()
            .map(indexInfoMapper::toResponse)
            .toList();

    boolean hasNext = (long) (pageNum + 1) * size < totalElements;

    String nextCursor = hasNext ? String.valueOf(pageNum + 1) : null;

    return new CursorPageResponse<>(
            content,
            nextCursor,
            null,
            size,
            totalElements,
            hasNext
    );
  }

  @Transactional(readOnly = true)
  public List<IndexInfoSummaryDto> getIndexInfoSummaries() {
    return indexInfoMapper.toSummaryDtoList(indexInfoRepository.findAll());
  }
}