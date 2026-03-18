package com.sprint.findex.domain.integration.entity;

import com.sprint.findex.common.entity.BaseEntity;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "integration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Integration extends BaseEntity {

    /** IndexInfo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    /** JobType */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20, nullable = false)
    private JobType jobType;

    /** 대상 날짜 */
    @Column(name = "target_date")
    private LocalDate targetDate;

    /** 작업자 */
    @Column(name = "worker", length = 100, nullable = false)
    private String worker;

    /** 작업 일시 */
    @Column(name = "job_time", nullable = false)
    private LocalDateTime jobTime;

    /** 결과 */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20, nullable = false)
    private JobResult result;

    // ⭐처리된 데이터 개수를 담을 필드입니다.
    @Column(name = "processed_count")
    private Integer processedCount;

    // ⭐이제 성공/실패와 '개수'를 세트로 받습니다!
    public void updateResult(JobResult result, Integer processedCount) {
        this.result = result;
        this.processedCount = processedCount;
    }

    public void updateResult(JobResult result) {
        this.result = result;
        this.processedCount = 0;
    }
}