package com.sprint.findex.domain.indexdata.mapper;

import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexDataMapper {

  /**
   * Request -> Entity로 조립하는 기능
   */
  public IndexData toEntity(IndexDataCreateRequest request, IndexInfo indexInfo) {
    return IndexData.builder()
        .indexInfo(indexInfo) // 어떤 지수인지(코스피 등) 연결
        .baseDate(request.getBaseDate())
        .sourceType(request.getSourceType())
        .openingPrice(request.getOpeningPrice())
        .closingPrice(request.getClosingPrice())
        .highPrice(request.getHighPrice())
        .lowPrice(request.getLowPrice())
        .versus(request.getVersus())
        .fluctuationRate(request.getFluctuationRate())
        .tradingQuantity(request.getTradingQuantity())
        .tradingPrice(request.getTradingPrice())
        .marketCapitalization(request.getMarketCapitalization())
        .build();
  }

  /**
   * 실제 재료(Entity) -> 영수증(Response)으로 포장하는 기능
   */
  public IndexDataResponse toResponse(IndexData entity) {
    return IndexDataResponse.builder()
        .id(entity.getId()) // DB에서 받은 번호표 달아주기
        .baseDate(entity.getBaseDate())
        .sourceType(entity.getSourceType())
        .openingPrice(entity.getOpeningPrice())
        .closingPrice(entity.getClosingPrice())
        .highPrice(entity.getHighPrice())
        .lowPrice(entity.getLowPrice())
        .versus(entity.getVersus())
        .fluctuationRate(entity.getFluctuationRate())
        .tradingQuantity(entity.getTradingQuantity())
        .tradingPrice(entity.getTradingPrice())
        .marketCapitalization(entity.getMarketCapitalization())
        .build();
  }
}