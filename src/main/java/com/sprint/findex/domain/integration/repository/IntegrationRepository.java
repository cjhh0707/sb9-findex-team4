package com.sprint.findex.domain.integration.repository;

import com.sprint.findex.domain.integration.entity.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, Long>, IntegrationRepositoryCustom {
    // 기본적인 save, findById 등은 JpaRepository가 알아서 제공
}