package com.sprint.findex.domain.indexinfo.dto;

/**목록 조회 및 페이징을 위한 검색 DTO*/
public record IndexInfoSearchCondition(
   String indexClassificationName, //지수 분류명 검색_ 부분 일치
   String indexName, //지수명 검색_ 부분 일치
   Boolean favorite, //즐겨찾기 필터_ 완전 일치
   String sortBy, //정렬 기준 필드명
   Long lastId, //No-offset페이징을 위한 이전 페이지 마지막 ID
   Integer pageSize //한 번에 가져올 데이터 개수
) {}
