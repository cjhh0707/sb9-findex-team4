package com.sprint.findex.domain.autointegration.service;


import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.entity.AutoSyncSetting;
import com.sprint.findex.domain.autointegration.repository.AutoSyncSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutoSyncSettingService {

  private final AutoSyncSettingRepository autoSyncSettingRepository;


//    새로운 자동 연동 설정 생성
  public AutoIntegration createAutoSyncSetting(Long indexInfoId) {
    AutoIntegration setting = AutoIntegration.builder()
        .indexInfoId(indexInfoId)
        .enabled(false) // 기본값 비활성화
        .build();

    return autoSyncSettingRepository.save(setting);
  }


//   ID로 조회
  public Optional<AutoIntegration> getAutoSyncSetting(Long id) {
    return autoSyncSettingRepository.findById(id);
  }

//   대상 식별자 조회
  public Optional<AutoIntegration> getAutoSyncSettingByIndexId(Long indexInfoId) {
    return autoSyncSettingRepository.findByIndexInfoId(indexInfoId);
  }


//    활성화 상태 기준 전체 조회
  public List<AutoIntegration> getAllByEnabled(boolean enabled) {
    return autoSyncSettingRepository.findAllByEnabled(enabled);
  }


//   활성화 상태 업데이트
  public AutoIntegration updateEnabled(Long id, boolean enabled) {
    AutoIntegration setting = autoSyncSettingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not found for id: " + id));

    setting.setEnabled(enabled);
    return autoSyncSettingRepository.save(setting);
  }


//   마지막 동기화 날짜 업데이트
  public AutoIntegration updateLastSyncDate(Long id, java.time.LocalDateTime lastSyncDate) {
    AutoIntegration setting = autoSyncSettingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoSyncSetting not found for id: " + id));

    setting.setLastSyncDate(lastSyncDate);
    return autoSyncSettingRepository.save(setting);
  }

}