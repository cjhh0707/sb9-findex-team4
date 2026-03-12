package com.sprint.findex.domain.integration.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_job_history")
@Getter
public class SyncJob {

  // 1. 기본 속성들
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;                      // ID

  @Column(name = "target_date")
  private LocalDate targetDate;         // 대상 날짜

  @Column(name = "worker")
  private String worker;                // 작업자

  @Column(name = "job_time")
  private LocalDateTime jobTime;        // 작업 일시

  // 2. 따로 만든 파일들을 연결(Mapping) 해주는 부분

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type")
  private JobType jobType;              // {유형} Enum 연결

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private JobStatus status;             // {결과} Enum 연결

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id")
  private IndexInfo indexInfo;          // {지수} 엔티티 연결 (FK)
}
