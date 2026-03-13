package com.sprint.findex.domain.autointegration.repository;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

// IndexInfo 테이블을 조회하는 레포
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
}
