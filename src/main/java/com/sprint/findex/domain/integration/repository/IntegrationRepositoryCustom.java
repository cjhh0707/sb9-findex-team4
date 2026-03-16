package com.sprint.findex.domain.integration.repository;


import com.sprint.findex.domain.integration.dto.request.IntegrationSearchCondition;
import com.sprint.findex.domain.integration.entity.Integration;

import java.util.List;

public interface IntegrationRepositoryCustom {
    // 검색 조건 DTO를 받아서 조건에 맞는 Entity 리스트를 반환하는 메서드
    List<Integration> findIntegrationsByCondition(IntegrationSearchCondition condition);
}