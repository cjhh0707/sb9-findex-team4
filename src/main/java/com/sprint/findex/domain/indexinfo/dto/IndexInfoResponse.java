package com.sprint.findex.domain.indexinfo.dto;

import com.sprint.findex.common.type.SourceType;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.math.BigDecimal;

/**지수 정보를 보여줄 때(조회할 때) 반환하는 DTO*/
public record IndexInfoResponse(
    Long id,
    String indexClassificationName,
    String indexName,
    Integer employedItemsCount,
    String basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    Boolean favorite
) {
    public static IndexInfoResponse from(IndexInfo indexInfo){
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
