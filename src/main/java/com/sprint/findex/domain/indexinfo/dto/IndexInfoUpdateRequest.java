package com.sprint.findex.domain.indexinfo.dto;

//기존 지수 정보의 세부 수치를 수정할 때 사용하는 DTO
public record IndexInfoUpdateRequest(
    Integer epu
){}
