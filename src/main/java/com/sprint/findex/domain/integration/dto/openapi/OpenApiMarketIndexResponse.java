package com.sprint.findex.domain.integration.dto.openapi;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OpenApiMarketIndexResponse {

  private Response response;

  @Getter
  @Setter
  public static class Response {
    private Header header;
    private Body body;
  }

  @Getter
  @Setter
  public static class Header {
    private String resultCode;
    private String resultMsg;
  }

  @Getter
  @Setter
  public static class Body {
    private int numOfRows;
    private int pageNo;
    private int totalCount;
    private Items items;
  }

  @Getter
  @Setter
  public static class Items {
    private List<Item> item; // 실제 지수 데이터들이 담긴 배열
  }

  //실제 필요한 알맹이 데이터
  @Getter
  @Setter
  public static class Item {
    private String basDt;  // 기준일자
    private String idxNm;  // 지수명 (ex: 코스피)
    private String idxCsf; // 지수분류
    private String clpr;   // 종가
    private String fltRt;  // 등락률
  }
}