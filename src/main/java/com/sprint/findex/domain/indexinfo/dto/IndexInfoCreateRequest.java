package com.sprint.findex.domain.indexinfo.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;

/**새로운 지수 정보를 등록할 때 사용하는 DTO */
public record IndexInfoCreateRequest(
  String indexClassificationName,
  String indexName,
  Integer employedItemsCount,
  String basePointInTime,
  BigDecimal baseIndex,
  SourceType sourceType,
  Boolean favorite
) {}


