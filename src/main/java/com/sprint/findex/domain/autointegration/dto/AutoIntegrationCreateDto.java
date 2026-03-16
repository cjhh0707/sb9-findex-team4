package com.sprint.findex.domain.autointegration.dto;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;

// 자동 연동 설정 생성용 DTO
public record AutoIntegrationCreateDto(
    Long indexInfoId// 연동 대상 식별자
) {}
