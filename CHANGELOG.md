# Changelog - calisat-ms-inventario

## [1.1.1] - 2026-09-23

### Fixed
- Validación JWT corregida con `DelegatingOAuth2TokenValidator` combinando issuer + audience
- Agregado `AudienceValidator` para validar el claim `aud` del JWT
- `SecurityConfig` ahora usa `withIssuerLocation` para autodescubrimiento correcto del JWK Set
- Corregida la compilación de los tests con `Pageable`/`PageImpl` en `StockControllerTest`
- Versión pom.xml actualizada a 1.1.1

[1.1.1]: https://github.com/DavNat13/calisat-ms-inventario/compare/v1.0.0...v1.1.1
