package com.sprint.findex.domain.autointegration.service;


import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.mapper.AutoIntergrationMapper;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.autointegration.repository.IndexInfoRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutoIntegrationService {

  private final AutoIntegrationRepository autoIntegrationRepository;
  private final AutoIntergrationMapper autoIntergrationMapper;
  private final IndexInfoRepository indexInfoRepository;


//    새로운 자동 연동 설정 생성
  public AutoIntegrationDto createAutoIntegration (Long indexInfoId) {
    IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
        .orElseThrow(() -> new IllegalArgumentException("IndexInfo not found"));
    AutoIntegration setting = AutoIntegration.builder()
        .indexInfo(indexInfo)
        .enabled(false) // 기본값 비활성화
        .build();

    AutoIntegration saved = autoIntegrationRepository.save(setting);
    return autoIntergrationMapper.toDto(saved);
  }


//   ID로 조회
  public AutoIntegrationDto getAutoIntegration(Long id) {
    AutoIntegration entity = autoIntegrationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not for id"));
    return autoIntergrationMapper.toDto(entity);
  }

//   대상 식별자 조회
public AutoIntegrationDto getAutoIntegrationByIndexId(Long indexInfoId) {
  AutoIntegration entity = autoIntegrationRepository.findByIndexInfoId(indexInfoId)
      .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for: " + indexInfoId));

  return autoIntergrationMapper.toDto(entity);
}


//    활성화 상태 기준 전체 조회
  public List<AutoIntegrationDto> getAllByEnabled(boolean enabled) {
    return autoIntegrationRepository.findAllByEnabled(enabled)
        .stream()
        .map(autoIntergrationMapper::toDto)
        .toList();
  }


//   활성화 상태 업데이트
  public AutoIntegrationDto updateEnabled(Long id, boolean enabled) {
    AutoIntegration setting = autoIntegrationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for id: " + id));

    setting.changeEnabled(enabled);
    return autoIntergrationMapper.toDto(setting);
  }


//   마지막 동기화 날짜 업데이트
  public AutoIntegrationDto updateLastIntegrationDate(Long id, java.time.LocalDateTime lastSyncDate) {
    AutoIntegration setting = autoIntegrationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for id: " + id));

    setting.updateLastIntegrationAt(LocalDateTime.now());
    return autoIntergrationMapper.toDto(setting);
  }

}