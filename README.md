# Findex

> 가볍고 빠른 외부 API 연동 금융 분석 도구

금융위원회 주가 지수 데이터를 Open API로 연동하여 시계열 차트와 성과 분석 정보를 제공하는 금융 특화 대시보드 서비스입니다.

- **노션 문서**: https://www.notion.so/d79e85f3ada9827e88c801dc8807d4fe?source=copy_link
- **프로젝트 기간**: 2026.03.11 ~ 2026.03.20

---

## 팀원 구성

| 이름 | GitHub |
|------|--------|
| 최재훈 | https://github.com/cjhh0707 |
| 정혁조 | https://github.com/hyeokjo-7 |
| 김사연 | https://github.com/KimSayeon |
| 최건위 | https://github.com/geoni-98 |
| 서현하 | https://github.com/by15622 |

---

## 기술 스택

| 분류 | 기술                          |
|------|-----------------------------|
| Language | Java 17                      |
| Framework | Spring Boot                 |
| Database | PostgreSQL                  |
| ORM | Spring Data JPA             |
| API 문서 | springdoc-openapi (Swagger) |
| 매핑 | MapStruct                   |
| 스케줄링 | Spring Scheduler            |
| 테스팅 | Postman, Swagger            |
| 배포 | Railway                     |
| 협업 | GitHub, Notion              |

---

## 팀원별 구현 기능

### 최재훈 - 대시보드 관리

1. **주요 지수 현황 요약**
   - 즐겨찾기 지수의 현재가, 대비, 등락률 조회
   - 일/주/월 단위 성과 비교

2. **지수 차트**
   - 월/분기/년 단위 시계열 종가 데이터 제공
   - 5일·20일 이동평균선(MA5, MA20) 계산 및 제공

3. **지수 성과 분석 랭킹**
   - 전일/전주/전월 대비 등락률 기준 랭킹 조회
   - 특정 지수의 순위 조회 지원

---

### 정혁조 - 자동 연동 설정 관리

1. **자동 연동 설정 수정**
   - 활성화/비활성화 상태 변경

2. **자동 연동 설정 목록 조회**
   - 지수, 활성화 여부 필터링
   - 커서 기반 페이지네이션

3. **배치 자동 연동**
   - Spring Scheduler를 활용한 일 1회 자동 데이터 수집
   - 마지막 연동 이후 최신 날짜까지 자동 범위 처리

---

### 김사연 - 지수 정보 관리

1. **지수 정보 등록 / 수정 / 삭제**
   - 지수 분류명, 지수명, 채용 종목 수, 기준 시점, 기준 지수, 즐겨찾기 관리
   - 삭제 시 연관 지수 데이터 함께 삭제

2. **지수 정보 목록 조회**
   - 지수 분류명, 지수명(부분 일치), 즐겨찾기 필터링
   - 커서 기반 페이지네이션 및 다중 정렬 지원

3. **Open API 연동을 통한 자동 등록/수정**

---

### 최건위 - 연동 작업 관리

1. **지수 정보 연동**
   - Open API 기반 지수 정보 자동 등록 및 수정

2. **지수 데이터 연동**
   - 지수 및 날짜 범위 지정 연동

3. **연동 작업 목록 조회**
   - 유형, 지수, 날짜, 작업자, 결과, 작업 일시 필터링
   - 커서 기반 페이지네이션

---

### 서현하 - 지수 데이터 관리

1. **지수 데이터 등록 / 수정 / 삭제**
   - 시가, 종가, 고가, 저가, 대비, 등락률, 거래량, 거래대금, 상장 시가 총액 관리

2. **지수 데이터 목록 조회**
   - 지수, 날짜 범위 필터링
   - 커서 기반 페이지네이션 및 다중 정렬 지원

3. **CSV Export**
   - 필터링·정렬 조건 적용 후 CSV 파일로 다운로드

---

## 파일 구조

```
src
└── main
    └── java
        └── com.sprint.findex
            ├── FindexApplication.java
            ├── common
            │   ├── dto
            │   │   ├── CursorPageResponse.java
            │   │   └── ErrorResponse.java
            │   ├── entity
            │   │   └── BaseEntity.java
            │   ├── exception
            │   │   └── GlobalExceptionHandler.java
            │   └── type
            │       └── SourceType.java
            └── domain
                ├── autointegration
                │   ├── controller  │  AutoIntegrationController.java
                │   ├── dto         │  AutoIntegrationCreateDto.java / AutoIntegrationDto.java / AutoIntegrationUpdateDto.java
                │   ├── entity      │  AutoIntegration.java
                │   ├── mapper      │  AutoIntergrationMapper.java
                │   ├── repository  │  AutoIntegrationRepository.java
                │   ├── scheduler   │  AutoIntegrationScheduler.java
                │   └── service     │  AutoIntegrationService.java
                ├── dashboard
                │   ├── controller  │  DashboardController.java
                │   ├── dto         │  ChartPeriodType.java / PeriodType.java
                │   │   └── response│  ChartDataPoint.java / IndexChartDto.java / IndexPerformanceDto.java / RankedIndexPerformanceDto.java
                │   └── service     │  DashboardService.java
                │       └── basic   │  BasicDashboardService.java
                ├── indexdata
                │   ├── controller  │  IndexDataController.java
                │   ├── dto         │  IndexDataCreateRequest.java / IndexDataResponse.java / IndexDataSearchCondition.java / IndexDataUpdateRequest.java
                │   ├── entity      │  IndexData.java
                │   ├── mapper      │  IndexDataMapper.java
                │   ├── repository  │  IndexDataRepository.java
                │   └── service     │  IndexDataService.java
                ├── indexinfo
                │   ├── controller  │  IndexInfoApi.java / IndexInfoController.java
                │   ├── dto         │  IndexInfoCreateRequest.java / IndexInfoResponse.java / IndexInfoSearchCondition.java / IndexInfoSummaryDto.java / IndexInfoUpdateRequest.java
                │   ├── entity      │  IndexInfo.java
                │   ├── mapper      │  IndexInfoMapper.java
                │   ├── repository  │  IndexInfoRepository.java
                │   └── service     │  IndexInfoService.java
                ├── integration
                │   ├── config      │  IntegrationConfig.java
                │   ├── controller  │  IntegrationController.java
                │   ├── dto
                │   │   ├── request │  IntegrationSearchCondition.java / IntegrationSyncRequest.java
                │   │   └── response│  IntegrationResponse.java
                │   ├── entity      │  Integration.java / JobResult.java / JobType.java
                │   ├── mapper      │  IntegrationMapper.java
                │   ├── repository  │  IntegrationRepository.java
                │   └── service     │  IntegrationService.java
                └── openapi
                    ├── ExternalApiService.java
                    └── dto         │  OpenApiItem.java / OpenApiResponse.java
```

---

## 구현 페이지

- **배포 링크**: sb9-findex-team4-production.up.railway.app

---

## 프로젝트 회고록

- **발표 자료**: https://drive.google.com/file/d/1RCX_tXjUBvLXkF3C-5qaOLKPVY8CvLXx/view
