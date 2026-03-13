package com.sprint.findex.domain.indexinfo.repository;

import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * [임시] 팀원 작업 전까지 에러 방지를 위해 만든 레포지토리입니다.
 */
@Repository
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {
}