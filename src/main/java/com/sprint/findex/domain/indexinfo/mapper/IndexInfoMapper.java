package com.sprint.findex.domain.indexinfo.mapper;

import com.sprint.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexInfoMapper {
  public IndexInfo toEntity(IndexInfoCreateRequest request) {
    return IndexInfo.builder()
        .indexClassificationName(request.indexClassificationName())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .favorite(request.favorite())
        .build();
  }
  public IndexInfoResponse toResponse(IndexInfo indexInfo) {
    return new IndexInfoResponse(
        indexInfo.getId(),
        indexInfo.getIndexClassificationName(),
        indexInfo.getIndexName(),
        indexInfo.getEmployedItemsCount(),
        indexInfo.getBasePointInTime(),
        indexInfo.getBaseIndex(),
        indexInfo.getSourceType(),
        indexInfo.getFavorite()
    );
  }

}
