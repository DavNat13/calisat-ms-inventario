-- ESQUEMA PRODUCTO (calisat-ms-inventario)
-- Migracion inicial. Flyway es el unico dueno del schema (ddl-auto=validate).
-- Tipos alineados con la entidad JPA Producto (Spring Boot 4.1 / Hibernate 6 / PostgreSQL).

CREATE TABLE producto (
    id              UUID PRIMARY KEY,
    sku             VARCHAR(64)  NOT NULL,
    nombre          VARCHAR(255) NOT NULL,
    categoria       VARCHAR(100),
    descripcion     VARCHAR(1000),
    precio          NUMERIC(10, 2),
    stock           INTEGER      NOT NULL DEFAULT 0,
    unidad_medida   VARCHAR(50),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP(6),
    CONSTRAINT uk_producto_sku UNIQUE (sku)
);
