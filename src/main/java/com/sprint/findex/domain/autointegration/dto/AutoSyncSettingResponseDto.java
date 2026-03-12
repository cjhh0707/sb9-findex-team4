package com.sprint.findex.domain.autointegration.dto;

import java.time.LocalDateTime;

// 응답용 DTO
public record AutoSyncSettingResponseDto (

  Long id, // 엔티티 PK

  Long indexInfoId, // 연동 대상 식별자

  boolean enabled, // 활성화 여부

  LocalDateTime lastSyncDate, // 마지막 연동 시간

  LocalDateTime createdAt, // 생성 시간

  LocalDateTime updatedAt // 마지막 수정 시간
) {}
