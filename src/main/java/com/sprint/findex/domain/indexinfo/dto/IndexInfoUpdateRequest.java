package com.sprint.findex.domain.indexinfo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**기존 지수 정보의 세부 수치를 수정할 때 사용하는 DTO */
public record IndexInfoUpdateRequest(
        Integer employedItemsCount,
        LocalDate basePointInTime,
        BigDecimal baseIndex,
        Boolean favorite
) {}
