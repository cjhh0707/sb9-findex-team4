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
// ⭐ [추가] 클래스 레벨에 readOnly 트랜잭션을 걸어주면 모든 조회 메서드에서 에러가 안 납니다!
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
            .enabled(false) // 기본값 비활성화
            .build();

    AutoIntegration saved = autoIntegrationRepository.save(setting);
    return autoIntergrationMapper.toDto(saved);
  }

  // ID로 조회
  public AutoIntegrationDto getAutoIntegration(Long id) {
    AutoIntegration entity = autoIntegrationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("AutoIntegration not for id"));
    return autoIntergrationMapper.toDto(entity);
  }

  // 대상 식별자 조회
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
    if (cursor != null && !cursor.isBlank()) {
      idAfter = Long.parseLong(cursor);
    }

    // size + 1 조회 후 다음 페이지 확인 (정렬은 repository 쿼리의 ORDER BY a.id ASC 사용)
    Pageable pageable = PageRequest.of(0, size + 1);

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

    // ⭐ 수정: nextCursor 대신 nextIdAfter 변수를 선언하고 ID 값을 바로 담습니다.
    Long nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      AutoIntegrationDto last = content.get(content.size() - 1);
      nextIdAfter = last.id(); // DTO의 id() 메서드를 통해 Long 타입 ID를 가져옵니다.
    }

    return new CursorPageResponse<>(
            content,       // 1. content: DTO 리스트
            null,          // 2. nextCursor: 문자열 커서 미사용
            nextIdAfter,   // 3. nextIdAfter: 다음 조회를 위한 커서 ID (이제 정상 동작합니다!)
            size,          // 4. size: 요청 페이지 크기
            0L,          // 5. totalElements: 전체 요소 개수 생략
            hasNext        // 6. hasNext: 다음 페이지 존재 여부
    );
  }

  // 활성화 상태 업데이트
  // ⭐ [핵심 추가] 이 어노테이션이 있어야 스위치 변경 사항이 DB에 진짜로 저장됩니다!
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

    setting.updateLastIntegrationAt(LocalDateTime.now());
    return autoIntergrationMapper.toDto(setting);
  }

}