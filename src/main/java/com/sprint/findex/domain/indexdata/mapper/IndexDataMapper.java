package com.sprint.findex.domain.indexdata.mapper;

import com.sprint.findex.common.type.SourceType;
import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataListResponse;
import com.sprint.findex.domain.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexDataMapper {

  /**
   * 주문서(Record) -> Entity
   * record는 'get' 없이 필드명() 메서드를 사용합니다.
   */
  public IndexData toEntity(IndexDataCreateRequest request, IndexInfo indexInfo) {
    return IndexData.builder()
        .indexInfo(indexInfo)
        .baseDate(request.baseDate()) // .getBaseDate() -> .baseDate()
        .sourceType(SourceType.USER) // 직접 등록 시 기본값 USER 설정
        .marketPrice(request.marketPrice())
        .closingPrice(request.closingPrice())
        .highPrice(request.highPrice())
        .lowPrice(request.lowPrice())
        .versus(request.versus())
        .fluctuationRate(request.fluctuationRate())
        .tradingQuantity(request.tradingQuantity())
        .tradingPrice(request.tradingPrice())
        .marketTotalAmount(request.marketTotalAmount())
        .yearRecordHighPrice(request.yearRecordHighPrice())
        .yearRecordHighDate(request.yearRecordHighDate())
        .yearRecordLowPrice(request.yearRecordLowPrice())
        .yearRecordLowDate(request.yearRecordLowDate())
        .build();
  }

  /**
   * Entity -> 영수증(Record)
   * Entity는 일반 클래스라 그대로 .get...()을 쓰고, DTO 조립 시 .builder()를 씁니다.
   */
  public IndexDataResponse toResponse(IndexData entity) {
    return IndexDataResponse.builder()
            .id(entity.getId())
            .indexInfoId(entity.getIndexInfo().getId())
            .baseDate(entity.getBaseDate())
            .sourceType(entity.getSourceType())
            .marketPrice(entity.getMarketPrice())
            .closingPrice(entity.getClosingPrice())
            .highPrice(entity.getHighPrice())
            .lowPrice(entity.getLowPrice())
            .versus(entity.getVersus())
            .fluctuationRate(entity.getFluctuationRate())
            .tradingQuantity(entity.getTradingQuantity())
            .tradingPrice(entity.getTradingPrice())
            .marketTotalAmount(entity.getMarketTotalAmount())
            .build();
  }

  /**
   * [목록 포장] 실물(Entity) -> 목록용 요약 영수증(Record)
   */
  public IndexDataListResponse toListResponse(IndexData entity) {
    return IndexDataListResponse.builder()
        .id(entity.getId())
        .baseDate(entity.getBaseDate())
        .closingPrice(entity.getClosingPrice())
        .versus(entity.getVersus())
        .fluctuationRate(entity.getFluctuationRate())
        .build();
  }

  /**
   * [추가] 기존 엔티티의 값을 수정 요청된 데이터로 업데이트합니다.
   */
  public void updateEntityFromDto(IndexDataUpdateRequest request, IndexData entity) {
    entity.update(
            request.sourceType() != null ? request.sourceType() : entity.getSourceType(),
            request.marketPrice(),
            request.closingPrice(),
            request.highPrice(),
            request.lowPrice(),
            request.versus(),
            request.fluctuationRate(),
            request.tradingQuantity(),
            request.tradingPrice(),
            request.marketTotalAmount()
    );
  }
}
