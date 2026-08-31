# Changelog - calisat-ms-inventario

## [1.0.2] - 2026-08-30

### Changed
- docker-compose alineado a la documentacion: red `calisat-net` y puerto `8080:8080`

## [1.0.1] - 2026-08-30

### Added
- Entidad JPA Producto con SKU unico y baja logica (activo)
- ProductoRepository con consultas derivadas (findBySku, existsBySku, findByActivoTrue)
- ProductoService con CRUD completo y validacion de SKU duplicado
- ProductoController con endpoints REST /api/v1/inventario/productos
- Excepcion de negocio SkuDuplicadoException (SKU duplicado -> 400)

## [1.0.0] - 2026-08-30

### Added
- Microservicio calisat-ms-inventario con Spring Boot 4.1.0 y Java 21
- Docker Compose con PostgreSQL 15 y app Spring Boot
- SecurityConfig para validacion JWT de Azure AD
- Configuracion via variables de entorno (sin credenciales ni fallback)
- Health check via Spring Actuator
