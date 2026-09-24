# Changelog - calisat-ms-inventario

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

[1.2.0]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.1.1...v1.2.0
[1.1.1]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.0...v1.1.1
