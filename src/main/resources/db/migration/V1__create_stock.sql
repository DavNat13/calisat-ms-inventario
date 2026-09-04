-- ESQUEMA STOCK (calisat-ms-inventario)
-- Migracion inicial. Flyway es el unico dueno del schema (ddl-auto=validate).
-- Tipos alineados con la entidad JPA Stock (Spring Boot 4.1 / Hibernate 6 / PostgreSQL).

CREATE TABLE stock (
    id                  BIGSERIAL    PRIMARY KEY,
    sku                 VARCHAR(64)  NOT NULL,
    cantidad_disponible INTEGER      NOT NULL DEFAULT 0,
    cantidad_reservada  INTEGER      NOT NULL DEFAULT 0,
    fecha_actualizacion TIMESTAMP(6),
    CONSTRAINT uk_stock_sku UNIQUE (sku)
);