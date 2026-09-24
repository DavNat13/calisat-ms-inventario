# Changelog - calisat-ms-inventario

## [1.3.0] - 2026-09-24

### Added
- RBAC con Azure Entra ID: bean `JwtAuthenticationConverter` que extrae el claim `roles` del JWT con prefijo `ROLE_` (normalizado a mayúsculas)
- Escrituras de stock (`POST`, `PUT`, `DELETE` en `/api/v1/stock/**`) restringidas a `ADMINISTRADOR`
- Lecturas de stock y `/actuator/health` siguen disponibles para cualquier usuario autenticado / público según el inventario de endpoints
- Versión pom.xml actualizada a 1.3.0

## [1.2.0] - 2026-09-23

### Added
- Endpoint `GET /api/v1/stock/sku/{sku}` para buscar stock por SKU (404 si no existe)
- Endpoints `POST /api/v1/stock/{sku}/reservar`, `/{sku}/liberar` y `/{sku}/confirmar` para movimientos de stock
- DTO `StockMovimientoRequest` con validación de cantidad positiva
- Excepciones `StockConflictException` (409) y `StockInvalidoException` (400) manejadas en `GlobalExceptionHandler`
- Validación en `StockService.actualizar`: `cantidadReservada` no puede ser mayor que `cantidadDisponible`
- Versión pom.xml actualizada a 1.2.0

## [1.1.1] - 2026-09-23

### Fixed
- Validación JWT corregida con `DelegatingOAuth2TokenValidator` combinando issuer + audience
- Agregado `AudienceValidator` para validar el claim `aud` del JWT
- `SecurityConfig` ahora usa `withIssuerLocation` para autodescubrimiento correcto del JWK Set
- Corregida la compilación de los tests con `Pageable`/`PageImpl` en `StockControllerTest`
- Versión pom.xml actualizada a 1.1.1

## [1.0.24] - 2026-09-04

- Correcciones de seguridad, infraestructura y deuda técnica (asunto del commit `119da7c`).

## [1.0.23] - 2026-09-04

- Alinear infra con ms-usuarios: quitar Flyway (migración y dependencias, `ddl-auto=update`), servicio compose `postgres-db` con variables desde el entorno del SO, gitignore y enforcer (asunto del commit `2970d75`).

## [1.0.22] - 2026-09-04

- Guard test contra reintroducción del dominio Producto (asunto del commit `01699c5`).

## [1.0.21] - 2026-09-04

- Tests migrados a Stock: `StockServiceTest`, `StockControllerTest`, `StockResponseTest`, `SecurityConfigTest` (asunto del commit `06b5535`).

## [1.0.20] - 2026-09-04

- DTOs `StockRequest`/`StockResponse` y `StockController` con rutas `/api/v1/stock` (asunto del commit `653edb6`).

## [1.0.19] - 2026-09-04

- `StockRepository` (`findBySku`) y `StockService`: lógica sobre `cantidadDisponible`/`cantidadReservada`, sin baja lógica (asunto del commit `b7b2ff2`).

## [1.0.18] - 2026-09-04

- Entidad Stock + migración `V1__create_stock`: eliminar atributos de catálogo, id Long auto-incremental (asunto del commit `c20a47f`).

## [1.0.17] - 2026-09-01

- Alinear rutas a la pauta: `/api/v1/inventario/productos` → `/inventario` (asunto del commit `a2d50ca`).

## [1.0.16] - 2026-09-01

- DTO de request `ProductoRequest` para evitar mass assignment + Javadoc duplicado (asunto del commit `3cbd149`).

## [1.0.15] - 2026-09-01

- 201 Created con Location, tests de controller y seguridad, deps de test (asunto del commit `40f46a1`).

## [1.0.14] - 2026-09-01

- Restringir actuator a solo health y documentar CORS `allowCredentials` (asunto del commit `c618c8f`).

## [1.0.13] - 2026-08-31

- Limpiar deuda técnica (UUID, jjwt) (asunto del commit `828f444`).

## [1.0.12] - 2026-08-31

- DTO `ProductoResponse` en lugar de Map (asunto del commit `1473dc8`).

## [1.0.11] - 2026-08-31

- GET por id solo devuelve productos activos (asunto del commit `e5d3f17`).

## [1.0.10] - 2026-08-31

- Validación `@Valid` y reglas en actualizar producto (asunto del commit `df3db26`).

## [1.0.9] - 2026-08-31

- Reemplazar `ddl-auto:update` por Flyway/Liquibase (asunto del commit `dfe93da`).

## [1.0.8] - 2026-08-31

- Unicidad de SKU a prueba de concurrencia (asunto del commit `bcc13a8`).

## [1.0.7] - 2026-08-30

- Manejo global de errores con `@RestControllerAdvice` (asunto del commit `e58981c`).

## [1.0.6] - 2026-08-30

- Ignorar archivo `.env` con variables sensibles (asunto del commit `bce5a29`).

## [1.0.5] - 2026-08-30

- Validar unicidad de SKU también al actualizar producto (asunto del commit `1ace3d3`).

## [1.0.4] - 2026-08-30

- Exponer ms-inventario en puerto 8082 (asunto del commit `2bc0b29`).

## [1.0.3] - 2026-08-30

- Excluir CHANGELOG.md y ARBOL-GENEALOGICO.md del control de versiones (asunto del commit `b069b41`).

## [1.0.2] - 2026-08-30

- Alinear docker-compose a la doc: red `calisat-net` y puerto 8080 (asunto del commit `8f331ab`).

## [1.0.1] - 2026-08-30

- CRUD de Producto: entidad, repositorio, servicio y controlador REST (asunto del commit `ef80636`).

## [1.0.0] - 2026-08-30

- Microservicio calisat-ms-inventario con estructura base y JWT Azure AD (asunto del commit `b7cce8f`).

> Nota: hasta la 1.0.24 el pom.xml permaneció fijado en 1.0.0 por convención (commit `fcd37a3`); la versión real se documentaba en los commits y en este CHANGELOG. Las versiones 1.0.1–1.0.24 se han incorporado aquí de forma condensada a partir de los asuntos de los commits (era pre-CHANGELOG).

[1.3.0]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.24...v1.1.1
[1.0.24]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.23...v1.0.24
[1.0.23]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.22...v1.0.23
[1.0.22]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.21...v1.0.22
[1.0.21]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.20...v1.0.21
[1.0.20]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.19...v1.0.20
[1.0.19]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.18...v1.0.19
[1.0.18]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.17...v1.0.18
[1.0.17]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.16...v1.0.17
[1.0.16]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.15...v1.0.16
[1.0.15]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.14...v1.0.15
[1.0.14]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.13...v1.0.14
[1.0.13]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.12...v1.0.13
[1.0.12]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.11...v1.0.12
[1.0.11]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.10...v1.0.11
[1.0.10]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.9...v1.0.10
[1.0.9]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.8...v1.0.9
[1.0.8]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.7...v1.0.8
[1.0.7]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/DavNat13/calisat-ms-inventario/releases/tag/v1.0.0
