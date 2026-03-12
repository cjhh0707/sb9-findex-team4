package com.sprint.findex.domain.indexinfo.dto;

import com.sprint.findex.common.type.SourceType;
import java.math.BigDecimal;

//새로운 지수 정보를 등록할 때 클라이언트로 전달받는 DTO
public record IndexInfoCreateRequest(
  String idxCsf,
  String idxNm,
  Integer epyItmsCnt,
  BigDecimal basIdx,
  SourceType sourceType, //사용자가 직접 등록하는지, Open API를 통하는지 구분
  Boolean favorite
){}


//엔티티 소스타입,즐겨찾기 순서 바꾸기