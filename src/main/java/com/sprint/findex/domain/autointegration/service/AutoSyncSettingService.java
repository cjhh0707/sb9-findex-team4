package com.sprint.findex.domain.autointegration.service;



import com.sprint.findex.domain.autointegration.AutoSyncSetting;
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
  public AutoSyncSetting createAutoSyncSetting(Long indexInfoId) {
    AutoSyncSetting setting = AutoSyncSetting.builder()
        .indexInfoId(indexInfoId)
        .enabled(false) // 기본값 비활성화
        .build();

    return autoSyncSettingRepository.save(setting);
  }


//   ID로 조회
  public Optional<AutoSyncSetting> getAutoSyncSetting(Long id) {
    return autoSyncSettingRepository.findById(id);
  }

//   대상 식별자 조회
  public Optional<AutoSyncSetting> getAutoSyncSettingByIndexId(Long indexInfoId) {
    return autoSyncSettingRepository.findByIndexInfoId(indexInfoId);
  }


//    활성화 상태 기준 전체 조회
  public List<AutoSyncSetting> getAllByEnabled(boolean enabled) {
    return autoSyncSettingRepository.findAllByEnabled(enabled);
  }


//   활성화 상태 업데이트
  public AutoSyncSetting updateEnabled(Long id, boolean enabled) {
    AutoSyncSetting setting = autoSyncSettingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoSyncSetting not found for id: " + id));

    setting.setEnabled(enabled);
    return autoSyncSettingRepository.save(setting);
  }


//   마지막 동기화 날짜 업데이트
  public AutoSyncSetting updateLastSyncDate(Long id, java.time.LocalDateTime lastSyncDate) {
    AutoSyncSetting setting = autoSyncSettingRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("AutoSyncSetting not found for id: " + id));

    setting.setLastSyncDate(lastSyncDate);
    return autoSyncSettingRepository.save(setting);
  }

}