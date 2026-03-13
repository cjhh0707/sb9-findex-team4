package com.sprint.findex.domain.indexinfo.service;

import com.sprint.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoSearchCondition;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.mapper.IndexInfoMapper;
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
  private final IndexInfoMapper indexInfoMapper;

  @Transactional
  public IndexInfoResponse createIndexInfo(IndexInfoCreateRequest request){
    //중복 검증
    if (indexInfoRepository.existsByIndexClassificationNameAndIndexName(
        request.indexClassificationName(), request.indexName())){
      throw new IllegalArgumentException("이미 등록된 지수 분류명과 지수명 조합입니다.");
    }

    IndexInfo indexInfo = indexInfoMapper.toEntity(request);

    IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

    // 요구사항의 "자동 연동 설정 정보도 같이 초기화되어야 합니다." 처리를 위해
    // 추후 AutoIntegrationRepository를 주입받아 비활성화 상태로 저장하는 로직 추가 필요

    return indexInfoMapper.toResponse(savedIndexInfo);
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

    return indexInfoMapper.toResponse(indexInfo);
  }

  @Transactional
  public void deleteIndexInfo(Long id){
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(()-> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. ID: " + id));

    //스키마 파일에 ON DELETE CASCADE 덕분에 여기서 IndexInfo만 삭제해도 연관 데이터 자동으로 DB에서 삭제
    indexInfoRepository.delete(indexInfo);
  }

  @Transactional(readOnly = true)
  public List<IndexInfoResponse> getIndexInfoList(IndexInfoSearchCondition condition){
    //클라이언트가 pageSize 안보냈을 경우를 대비해 기본값 10
    int size = (condition.pageSize() != null) ? condition.pageSize() : 10;

    //첫번째 페이지(0)부터 size만큼 가져옴
    Pageable pageable = PageRequest.of(0, size);

    //레포지토리에 만들어둔 동적 검색 쿼리를 호출
    List<IndexInfo> indexInfos = indexInfoRepository.searchIndexInfos(
        condition.indexClassificationName(),
        condition.indexName(),
        condition.favorite(),
        condition.lastId(),
        pageable
    );

    return indexInfos.stream()
        .map(indexInfoMapper::toResponse)
        .toList();
  }

}
