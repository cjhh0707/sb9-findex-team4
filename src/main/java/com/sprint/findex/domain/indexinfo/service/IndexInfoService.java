package com.sprint.findex.domain.indexinfo.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.service.AutoIntegrationService;
import com.sprint.findex.domain.indexinfo.dto.*;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

  private final IndexInfoRepository indexInfoRepository;
  private final AutoIntegrationService autoIntegrationService;

  @Transactional
  public IndexInfoResponse createIndexInfo(IndexInfoCreateRequest request){
    //중복 검증
    if (indexInfoRepository.existsByIndexClassificationNameAndIndexName(
        request.indexClassificationName(), request.indexName())){
      throw new IllegalArgumentException("이미 등록된 지수 분류명과 지수명 조합입니다.");
    }

    //DTO -> Entity
    IndexInfo indexInfo = IndexInfo.builder()
        .indexClassificationName(request.indexClassificationName())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .sourceType(request.sourceType())
        .favorite(request.favorite())
        .build();

    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    // 요구사항의 "자동 연동 설정 정보도 같이 초기화되어야 합니다." 처리를 위해
    // 추후 AutoIntegrationRepository를 주입받아 비활성화 상태로 저장하는 로직 추가 필요
    autoIntegrationService.createAutoIntegration(savedIndexInfo.getId());

    return IndexInfoResponse.from(savedIndexInfo);
  }

  @Transactional
  public IndexInfoResponse updateIndexInfo(Long id, IndexInfoUpdateRequest request){
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));

    // 정보 수정
    indexInfo.updateInfo(
        request.employedItemsCount(),
        request.basePointInTime(),
        request.baseIndex()
    );

    //즐겨찾기 상태 수정
    indexInfo.updateFavorite(request.favorite());

    //수정된 결과를 DTO로 변환하여 반환
    return IndexInfoResponse.from(indexInfo);
  }

  @Transactional
  public void deleteIndexInfo(Long id){
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));

    //스키마 파일에 ON DELETE CASCADE 덕분에 여기서 IndexInfo만 삭제해도 연관 데이터 자동으로 DB에서 삭제
    indexInfoRepository.delete(indexInfo);
  }

  // 지수 정보 목록 조회 (Cursor 기반 페이징 적용)
  @Transactional(readOnly = true)
  public CursorPageResponse<IndexInfoResponse> getIndexInfoList(IndexInfoSearchCondition condition, Long idAfter, int size) {
    Pageable pageable = PageRequest.of(0, size + 1);

    List<IndexInfo> indexInfos = indexInfoRepository.searchIndexInfos(
            condition.indexClassificationName(),
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

    // 공용 CursorPageResponse 규격에 맞춰 반환
    return new CursorPageResponse<>(
            content,      // 데이터 리스트
            null,         // 문자열 커서 사용 안 함
            nextIdAfter,  // 다음 조회를 위한 커서 ID
            size,         // 요청한 페이지 크기
            null,         // 전체 개수 (커서 페이징에선 생략)
            hasNext       // 다음 페이지 존재 여부
    );
  }

  /**
   * [신규 추가] 지수 정보 요약 목록 조회
   */
  @Transactional(readOnly = true)
  public List<IndexInfoSummaryDto> getIndexInfoSummaries() {
    // 모든 지수 정보를 가져와 요약 DTO로 변환하여 반환
    return indexInfoRepository.findAll().stream()
            .map(info -> new IndexInfoSummaryDto(
                    info.getId(),
                    info.getIndexClassificationName(),
                    info.getIndexName()
            ))
            .toList();
  }

}
