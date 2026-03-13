package com.sprint.findex.domain.indexdata.mapper;

import com.sprint.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.findex.domain.indexdata.entity.IndexData;
import com.sprint.findex.domain.indexinfo.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexDataMapper {

  /**
   * 주문서(Request)를 받아서 실제 DB에 저장할 재료(Entity)로 만듭니다.
   */
  public IndexData toEntity(IndexDataCreateRequest request, IndexInfo indexInfo) {
    return IndexData.builder()
        .indexInfo(indexInfo)            // 어떤 지수인지(코스피 등) 연결함
        .baseDate(request.getBaseDate()) // 날짜를 옮겨 담음
        .sourceType(request.getSourceType()) // 데이터 출처를 옮겨 담음
        .openingPrice(request.getOpeningPrice()) // 시가를 옮겨 담음
        .closingPrice(request.getClosingPrice()) // 종가를 옮겨 담음
        .highPrice(request.getHighPrice())       // 고가를 옮겨 담음
        .lowPrice(request.getLowPrice())         // 저가를 옮겨 담음
        .versus(request.getVersus())             // 전일 대비 차이를 옮겨 담음
        .fluctuationRate(request.getFluctuationRate()) // 등락률을 옮겨 담음
        .tradingQuantity(request.getTradingQuantity()) // 거래량을 옮겨 담음
        .tradingPrice(request.getTradingPrice())       // 거래 대금을 옮겨 담음
        .marketCapitalization(request.getMarketCapitalization()) // 시가 총액을 옮겨 담음
        .build();
  }

  /**
   * DB에서 꺼낸 재료(Entity)를 사용자가 보기 편한 영수증(Response)으로 만듭니다.
   */
  public IndexDataResponse toResponse(IndexData entity) {
    return IndexDataResponse.builder()
        .id(entity.getId())   // DB가 붙여준 고유 번호표를 영수증에 적음
        .baseDate(entity.getBaseDate())         // 저장된 날짜를 적음
        .sourceType(entity.getSourceType())     // 저장된 출처를 적음
        .openingPrice(entity.getOpeningPrice()) // 저장된 시가를 적음
        .closingPrice(entity.getClosingPrice()) // 저장된 종가를 적음
        .highPrice(entity.getHighPrice())       // 저장된 고가를 적음
        .lowPrice(entity.getLowPrice())         // 저장된 저가를 적음
        .versus(entity.getVersus())             // 저장된 대비치를 적음
        .fluctuationRate(entity.getFluctuationRate()) // 저장된 등락률을 적음
        .tradingQuantity(entity.getTradingQuantity()) // 저장된 거래량을 적음
        .tradingPrice(entity.getTradingPrice())       // 저장된 거래 대금을 적음
        .marketCapitalization(entity.getMarketCapitalization()) // 저장된 시가 총액을 적음
        .build();
  }
}