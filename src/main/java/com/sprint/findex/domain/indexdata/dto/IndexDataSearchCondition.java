package com.sprint.findex.domain.indexdata.dto;

import java.time.LocalDate;

/**
 * 지수 데이터 목록 조회 및 필터링을 위한 검색 DTO
 */
public record IndexDataSearchCondition(
        LocalDate startDate,  // 시작 날짜
        LocalDate endDate,    // 종료 날짜
        Long indexInfoId,     // 조회할 지수 정보의 고유 ID
        Long lastId,
        Integer pageSize
) {}