package com.sprint.findex.domain.autointegration.repository;

import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// IndexInfo 테이블을 조회하는 레포
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

}
