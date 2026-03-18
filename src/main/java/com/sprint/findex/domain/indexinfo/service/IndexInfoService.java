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
  private final IndexInfoMapper indexInfoMapper; // Mapper 의존성 추가

  @Transactional
  public IndexInfoResponse createIndexInfo(IndexInfoCreateRequest request) {
    // 1. 중복 검증
    if (indexInfoRepository.existsByIndexClassificationAndIndexName(
            request.indexClassification(), request.indexName())) {
      throw new IllegalArgumentException("이미 등록된 지수 분류명과 지수명 조합입니다.");
    }

    // 2. DTO -> Entity (Mapper 활용으로 코드가 대폭 감소했습니다)
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

    // Entity 내부의 비즈니스 메서드를 통해 상태 변경
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

    // 다음 페이지 존재 여부를 확인하기 위해 size + 1만큼 조회
    Pageable pageable = PageRequest.of(pageNum, size + 1, sort);

    List<IndexInfo> indexInfos = indexInfoRepository.searchIndexInfos(
            condition.indexClassification(),
            condition.indexName(),
            condition.favorite(),
            pageable
    );

    boolean hasNext = indexInfos.size() > size;

    // Mapper를 활용한 리스트 변환
    List<IndexInfoResponse> content = indexInfos.stream()
            .limit(size)
            .map(indexInfoMapper::toResponse)
            .toList();

    long totalElements = indexInfoRepository.countIndexInfos(
            condition.indexClassification(),
            condition.indexName(),
            condition.favorite()
    );

    // nextCursor = 다음 페이지 번호 (문자열)
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
    // Mapper의 List 변환 메서드를 사용하여 불필요한 Stream 생성을 줄였습니다.
    return indexInfoMapper.toSummaryDtoList(indexInfoRepository.findAll());
  }

  @Transactional(readOnly = true)
  // 기존 List<IndexInfo> 반환 타입에서 내부 로직(toIndexInfoDto)에 맞춰 DTO 반환 타입으로 수정했습니다.
  public List<IndexInfoResponse> findAllByFavoriteTrue(Boolean favorite) {
    List<IndexInfo> indexInfos = indexInfoRepository.findAllByFavoriteTrue();
    return indexInfos.stream()
            .map(indexInfoMapper::toResponse)
            .toList();
  }
}