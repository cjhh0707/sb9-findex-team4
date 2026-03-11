package com.sprint.findex.domain.autointegration;

import java.time.LocalDateTime;
import java.util.UUID;

// 응답용 DTO
public record AutoSyncSettingResponseDto (
  UUID uuid,
  String indexId,
  boolean enabed,
  LocalDateTime lastSyncDate,
  LocalDateTime createdAt,
  LocalDateTime updateAt
) {}
