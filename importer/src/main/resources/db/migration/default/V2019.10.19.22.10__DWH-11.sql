DROP TABLE IF EXISTS fact_raw;

CREATE TABLE fact_raw
(
    id                   BIGSERIAL NOT NULL,
    transaction_id       VARCHAR(255),
    supplier_name        VARCHAR(255),
    business_unit        VARCHAR(255),
    origin_city          VARCHAR(255),
    origin_state         VARCHAR(255),
    origin_country       VARCHAR(255),
    origin_zip           VARCHAR(255),
    destination_city     VARCHAR(255),
    destination_state    VARCHAR(255),
    destination_country  VARCHAR(255),
    destination_zip      VARCHAR(255),
    zone                 VARCHAR(255),
    shipment_date        TIMESTAMP,
    delivery_date        TIMESTAMP,
    service_type         VARCHAR(255),
    billable_weight      FLOAT,
    actual_weight        FLOAT,
    length               FLOAT,
    width                FLOAT,
    height               FLOAT,
    cost                 DECIMAL,
    accessorial_service1 VARCHAR(255),
    accessorial_service2 VARCHAR(255),
    accessorial_service3 VARCHAR(255),
    accessorial_charge1  DECIMAL,
    accessorial_charge2  DECIMAL,
    accessorial_charge3  DECIMAL,
    discount             VARCHAR(255),
    distance             FLOAT,
    constraint fact_raw_uniq unique (ID)
);