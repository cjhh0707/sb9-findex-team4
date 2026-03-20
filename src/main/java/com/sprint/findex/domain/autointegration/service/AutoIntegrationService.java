package com.sprint.findex.domain.autointegration.service;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.mapper.AutoIntergrationMapper;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

import com.sprint.findex.domain.indexinfo.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AutoIntegrationService {

  private final AutoIntegrationRepository autoIntegrationRepository;
  private final AutoIntergrationMapper autoIntergrationMapper;
  private final IndexInfoRepository indexInfoRepository;

  // 새로운 자동 연동 설정 생성
  public AutoIntegrationDto createAutoIntegration (Long indexInfoId) {
    IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
            .orElseThrow(() -> new IllegalArgumentException("IndexInfo not found"));
    AutoIntegration setting = AutoIntegration.builder()
            .indexInfo(indexInfo)
            .enabled(false)
            .build();

    AutoIntegration saved = autoIntegrationRepository.save(setting);
    return autoIntergrationMapper.toDto(saved);
  }

  // 목록 조회
  public CursorPageResponse<AutoIntegrationDto> getAutoSyncConfigs(
          Long indexInfoId,
          Boolean enabled,
          Long idAfter,
          String cursor,
          String sortField,
          String sortDirection,
          int size
  ) {

    // cursor null 처리
    if (cursor != null && !cursor.isBlank()) {
      idAfter = Long.parseLong(cursor);
    }

    Pageable pageable = PageRequest.of(0, size + 1);

    List<AutoIntegration> list = autoIntegrationRepository.search(
            indexInfoId,
            enabled,
            idAfter,
            pageable
    );

    boolean hasNext = list.size() > size;

    List<AutoIntegrationDto> content = list.stream()
            .limit(size)
            .map(autoIntergrationMapper::toDto)
            .toList();

    Long nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      nextIdAfter = content.get(content.size() - 1).id();
    }

    return new CursorPageResponse<>(
            content,
            nextIdAfter != null ? String.valueOf(nextIdAfter) : null,
            nextIdAfter,
            size,
            0L,
            hasNext
    );
  }

  // 활성화 상태 업데이트
  @Transactional
  public AutoIntegrationDto updateEnabled(Long id, boolean enabled) {
    AutoIntegration setting = autoIntegrationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for id: " + id));

    setting.changeEnabled(enabled);
    return autoIntergrationMapper.toDto(setting);
  }

  // 마지막 동기화 날짜 업데이트
  public AutoIntegrationDto updateLastIntegrationDate(Long id, java.time.LocalDateTime lastSyncDate) {
    AutoIntegration setting = autoIntegrationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for id: " + id));

    setting.updateLastIntegrationAt(lastSyncDate);
    return autoIntergrationMapper.toDto(setting);
  }

}