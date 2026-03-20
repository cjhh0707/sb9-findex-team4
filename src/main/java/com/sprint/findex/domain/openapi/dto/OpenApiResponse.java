package com.sprint.findex.domain.openapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

/**
 * 공공데이터포털 API 공통 응답 래퍼 DTO
 * <pre>
 * {
 *   "response": {
 *     "header": { "resultCode": "00", "resultMsg": "NORMAL SERVICE." },
 *     "body": {
 *       "items": { "item": [...] },
 *       "numOfRows": 100,
 *       "pageNo": 1,
 *       "totalCount": 500
 *     }
 *   }
 * }
 * </pre>
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiResponse {

    private Response response;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        /** API가 결과 없을 때 null, 1건일 때 객체, 여러 건일 때 배열 반환할 수 있으므로 List로 처리 */
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<OpenApiItem> item;
    }
}
