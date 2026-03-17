-- 1. 지수 정보 (index_info)
CREATE TABLE IF NOT EXISTS index_info
(
    id                        BIGSERIAL    PRIMARY KEY,
    index_classification_name VARCHAR(100) NOT NULL,
    index_name                VARCHAR(255) NOT NULL,
    employed_items_count      INTEGER,
    base_point_in_time        DATE,
    base_index                DECIMAL,
    source_type               VARCHAR(20)  NOT NULL,
    favorite                  BOOLEAN      NOT NULL DEFAULT false,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    CONSTRAINT uk_index_info UNIQUE (index_classification_name, index_name)
    );

-- 2. 지수 데이터 (index_data)
CREATE TABLE IF NOT EXISTS index_data
(
    id                    BIGSERIAL   PRIMARY KEY,
    index_info_id         BIGINT      NOT NULL,
    base_date             DATE        NOT NULL,
    source_type           VARCHAR(20) NOT NULL,
    opening_price         DECIMAL,
    closing_price         DECIMAL,
    high_price            DECIMAL,
    low_price             DECIMAL,
    versus                DECIMAL,
    fluctuation_rate      DECIMAL,
    trading_quantity      BIGINT,
    trading_price         BIGINT,
    market_capitalization BIGINT,
    created_at            TIMESTAMP   NOT NULL,
    updated_at            TIMESTAMP   NOT NULL,
    CONSTRAINT uk_index_data UNIQUE (index_info_id, base_date),
    CONSTRAINT fk_index_data_index_info FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
    );

-- 3. 연동 작업 이력 (integration)
CREATE TABLE IF NOT EXISTS integration
(
    id            BIGSERIAL    PRIMARY KEY,
    index_info_id BIGINT,
    job_type      VARCHAR(20)  NOT NULL,
    target_date   DATE,
    worker        VARCHAR(100) NOT NULL,
    job_time      TIMESTAMP    NOT NULL,
    result        VARCHAR(20)  NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    CONSTRAINT fk_integration_index_info FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
    );

-- 4. 자동 연동 설정 (auto_integration)
CREATE TABLE IF NOT EXISTS auto_integration
(
    id                  BIGSERIAL PRIMARY KEY,
    index_info_id       BIGINT    NOT NULL,
    enabled             BOOLEAN   NOT NULL DEFAULT false,
    last_integration_at TIMESTAMP,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    CONSTRAINT uk_auto_integration UNIQUE (index_info_id),
    CONSTRAINT fk_auto_integration_index_info FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
    );