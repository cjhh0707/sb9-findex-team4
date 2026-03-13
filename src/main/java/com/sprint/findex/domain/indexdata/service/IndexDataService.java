package com.sprint.findex.domain.indexdata.service;

import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository; // 임시로 만들어둔 레포지토리
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository; // 임시로 만든 레포지토리
  private final IndexDataMapper indexDataMapper;

  /**
   * 지수 데이터 저장 (Create)
   */
  @Transactional
  public IndexDataResponse save(IndexDataCreateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(request.getIndexInfoId())
        .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다."));

    IndexData indexData = indexDataMapper.toEntity(request, indexInfo);

    IndexData savedData = indexDataRepository.save(indexData);

    // 저장된 데이터를 Response로 돌려준다.
    return indexDataMapper.toResponse(savedData);
  }
}