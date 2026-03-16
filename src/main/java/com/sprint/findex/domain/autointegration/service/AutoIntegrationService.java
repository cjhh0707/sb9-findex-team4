package com.sprint.findex.domain.autointegration.service;


import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.dto.CursorPageResponse;
import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.autointegration.mapper.AutoIntergrationMapper;
import com.sprint.findex.domain.autointegration.repository.AutoIntegrationRepository;
import com.sprint.findex.domain.autointegration.repository.IndexInfoRepository;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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
public CursorPageResponse<AutoIntegrationDto> getAutoSyncConfigs(
    Long indexInfoId,
    Boolean enabled,
    Long idAfter,
    String cursor, // 페이징 조회용
    String sortField,
    String sortDirection,
    int size
) {

// cursor null 처리
  if (cursor != null) {
    idAfter = Long.parseLong(cursor);
  }

  // 정렬 생성
  Sort sort = sortDirection.equalsIgnoreCase("desc")
      ? Sort.by(sortField).descending()
      : Sort.by(sortField).ascending();

  // size + 1 조회 후 다음 페이지 확인
  Pageable pageable = PageRequest.of(0, size + 1, sort);

  List<AutoIntegration> list = autoIntegrationRepository.search(
      indexInfoId,
      enabled,
      idAfter,
      pageable
  );

  // hasNext 판단
  boolean hasNext = list.size() > size;

  // 실제 반환 데이터
  List<AutoIntegrationDto> content = list.stream()
      .limit(size)
      .map(autoIntergrationMapper::toDto)
      .toList();

  String nextCursor = null;
  if (hasNext) {
    AutoIntegrationDto last = content.get(content.size() - 1);
    nextCursor = String.valueOf(last.id());
  }

  return new CursorPageResponse<>(
      content,
      nextCursor,
      size,
      hasNext
  );
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