package com.sprint.findex.domain.indexinfo.dto;

/**목록 조회 및 페이징을 위한 검색 DTO*/
public record IndexInfoSearchCondition(
        String indexClassification,   // 지수 분류명 검색 (부분 일치)
        String indexName,             // 지수명 검색 (부분 일치)
        Boolean favorite,             // 즐겨찾기 필터 (완전 일치)
        String sortField,             // 정렬 기준 필드명
        String sortDirection          // 정렬 방향 (asc/desc)
) {}
