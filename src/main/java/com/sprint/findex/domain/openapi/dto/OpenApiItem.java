package com.sprint.findex.domain.openapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * 공공데이터포털 금융위원회 주가지수 API 응답 아이템 DTO
 * data.go.kr API는 숫자값도 문자열로 반환하므로 String 타입 사용
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiItem {

    /** 기준일자 (YYYYMMDD) */
    private String basDt;

    /** 지수분류명 */
    private String idxCsf;

    /** 지수명 */
    private String idxNm;

    /** 채용종목수 */
    private String epyItmsCnt;

    /** 기준시점 (YYYY.MM.DD 형식) */
    private String basPnt;

    /** 기준지수 */
    private String basIdx;

    /** 시가 */
    private String mkp;

    /** 종가 */
    private String clpr;

    /** 고가 */
    private String hipr;

    /** 저가 */
    private String lopr;

    /** 전일 대비 등락 */
    private String vs;

    /** 전일 대비 등락률 */
    private String fltRt;

    /** 거래량 */
    private String trqu;

    /** 거래대금 */
    private String trPrc;

    /** 상장시가총액 */
    private String lstgMrktTotAmt;
}
