package com.sprint.findex.domain.indexinfo.mapper;

import com.sprint.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.findex.domain.indexinfo.dto.IndexInfoSummaryDto;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

// unmappedTargetPolicy = ReportingPolicy.IGNORE를 설정하여 BaseEntity의 생성일/수정일 등 매핑되지 않는 필드 경고를 무시합니다.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndexInfoMapper {

    /**
     * Entity -> Response DTO
     * Entity의 favorite이 null일 경우 false로 기본값 설정 (IndexInfoResponse.from 로직 대체)
     */
    @Mapping(target = "favorite", source = "favorite", defaultValue = "false")
    IndexInfoResponse toResponse(IndexInfo indexInfo);

    /**
     * Service의 findAllByFavoriteTrue 메서드에서 사용된 메서드명 매핑
     * (반환 타입이 IndexInfoDto가 아닌 IndexInfoResponse라고 가정했습니다)
     */
    @Mapping(target = "favorite", source = "favorite", defaultValue = "false")
    IndexInfoResponse toIndexInfoDto(IndexInfo indexInfo);

    /**
     * Entity -> Summary DTO
     */
    IndexInfoSummaryDto toSummaryDto(IndexInfo indexInfo);

    /**
     * CreateRequest -> Entity
     * - id는 자동 생성이므로 무시
     * - sourceType은 'USER'로 고정 (Service 로직 대체)
     * - favorite이 null일 경우 기본값 false 적용
     */
    @Mapping(target = "sourceType", constant = "USER")
    @Mapping(target = "favorite", source = "favorite", defaultValue = "false")
    IndexInfo toEntity(IndexInfoCreateRequest request);

    /**
     * List 매핑 편의 메서드
     */
    List<IndexInfoSummaryDto> toSummaryDtoList(List<IndexInfo> indexInfos);
}