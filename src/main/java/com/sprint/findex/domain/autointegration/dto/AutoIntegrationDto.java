package com.sprint.findex.domain.autointegration.dto;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;

// 컨트롤러 <--> 서비스 간 데이터 전달용(엔티티 노출 X)
public record AutoIntegrationDto(
    Long id,
    IndexInfo indexInfo,
    boolean enabled
) {}
