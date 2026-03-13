package com.sprint.findex.domain.indexinfo.dto;

import com.sprint.findex.common.type.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**새로운 지수 정보를 등록할 때 사용하는 DTO */
public record IndexInfoCreateRequest(
    @NotBlank(message = "지수 분류명은 필수입니다.")
    String indexClassificationName,

    @NotBlank(message = "지수명은 필수입니다.")
    String indexName,

    @NotNull(message = "채용 종목 수는 필수입니다.")
    @PositiveOrZero(message = "채용 종목 수는 0개 이상이어야 합니다.")
    Integer employedItemsCount,

    @NotBlank(message = "기준 시점은 필수입니다.")
    String basePointInTime,

    @NotNull(message = "기준 지수는 필수입니다.")
    BigDecimal baseIndex,

    @NotNull(message = "소스 타입은 필수입니다.")
    SourceType sourceType,

    @NotNull(message = "즐겨찾기 여부는 필수입니다.")
    Boolean favorite

) {}


