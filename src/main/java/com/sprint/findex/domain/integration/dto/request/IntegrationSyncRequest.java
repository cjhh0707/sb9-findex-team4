package com.sprint.findex.domain.integration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationSyncRequest {
    private List<Long> indexInfoIds;  // 연동할 지수 정보 ID 목록
    private LocalDate baseDateFrom;   // 연동 시작 대상 날짜
    private LocalDate baseDateTo;     // 연동 종료 대상 날짜
}