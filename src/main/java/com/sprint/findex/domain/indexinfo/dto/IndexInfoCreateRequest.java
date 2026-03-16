package com.sprint.findex.domain.indexinfo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**새로운 지수 정보를 등록할 때 사용하는 DTO */
public record IndexInfoCreateRequest(
        @NotBlank(message = "지수 분류명은 필수입니다.")
        String indexClassification,

        @NotBlank(message = "지수명은 필수입니다.")
        String indexName,

        @NotNull(message = "채용 종목 수는 필수입니다.")
        @PositiveOrZero(message = "채용 종목 수는 0개 이상이어야 합니다.")
        Integer employedItemsCount,

        @NotNull(message = "기준 시점은 필수입니다.")
        LocalDate basePointInTime,

        @NotNull(message = "기준 지수는 필수입니다.")
        BigDecimal baseIndex,

        Boolean favorite
) {}
