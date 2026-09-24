# calisat-ms-inventario

> Microservicio Spring Boot de stock: consultas por SKU y movimientos transaccionales de reserva, liberación y confirmación de unidades.

![Versión](https://img.shields.io/badge/version-1.2.0-2563EB)
![Java](https://img.shields.io/badge/Java-21-F89820?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)
![Estado](https://img.shields.io/badge/estado-modo%20acad%C3%A9mico-FACC15)

**Versión actual: `1.2.0`** (definida en `pom.xml` · historial en [`CHANGELOG.md`](CHANGELOG.md))

---

## 📑 Índice

- [📋 Descripción general](#-descripción-general)
- [✨ Características principales](#-características-principales)
- [🏗️ Arquitectura](#-arquitectura)
- [🚀 Requisitos](#-requisitos)
- [⚙️ Configuración](#-configuración)
- [▶️ Ejecución local](#-ejecución-local)
- [📡 Endpoints principales](#-endpoints-principales)
- [🗃️ Modelo de datos](#-modelo-de-datos)
- [🔒 Seguridad](#-seguridad)
- [🧪 Tests](#-tests)
- [📦 Despliegue](#-despliegue)
- [🔗 Microservicios relacionados](#-microservicios-relacionados)
- [📄 Licencia y modo académico](#-licencia-y-modo-académico)

---

## 📋 Descripción general

**calisat-ms-inventario** gestiona el **stock de productos** de la plataforma Calisat. Mantiene, por cada SKU, las cantidades **disponible** y **reservada**, y expone operaciones de movimiento utilizadas por el flujo de compra:

- **Reservar** unidades al confirmar un carrito/orden.
- **Liberar** reservas al cancelar.
- **Confirmar** la salida física de unidades al pagar/despachar.

Se expone bajo `/api/v1/stock` con **PostgreSQL** como almacén y validación de **JWT (Microsoft Entra ID)** sin RBAC (*modo académico*). Las claves de integración (`ms-carrito`, `ms-orden`) lo consumen en el puerto **8083**.

## ✨ Características principales

- 📦 **Inventario por SKU**: registro único por SKU con cantidades disponible/reservada.
- 🔁 **Movimientos con control de conflictos**: `reservar`, `liberar` y `confirmar` devuelven `404` (SKU inexistente) o `409` (stock insuficiente / reservado insuficiente).
- ✅ **Validaciones de dominio**: cantidades no negativas; `cantidadReservada ≤ cantidadDisponible`.
- 🔎 **Consultas múltiples**: listado paginado, por `id` y por `sku` (búsqueda canónica).
- 🧾 **Manejo global de errores**: `GlobalExceptionHandler` con `SkuDuplicadoException`, `StockConflictException` (409) y `StockInvalidoException` (400).
- 🩺 **Actuator**: `health` e `info` con detalle cuando está autorizado.
- ⚙️ **Configuración 100 % por variables de entorno** (datasource y JWT sin secretos en el YAML).
- 🧪 **Suite amplia**: 41 tests unitarios (servicio, controladores, seguridad y dominio).
- 🐳 **Docker multi-stage** con usuario no root y health check.

## 🏗️ Arquitectura

```mermaid
flowchart LR
    C[calisat-ms-carrito] -->|GET /sku/{sku}| INV[calisat-ms-inventario<br/>:8083]
    O[calisat-ms-orden] -->|reservar / liberar / confirmar| INV
    INV --> PG[(PostgreSQL<br/>BD inventario)]
    INV --> AC[/actuator/health]
```

### Estructura de paquetes

```
com.califorge.msinventario
├── config/        # SecurityConfig (JWT + CORS)
├── controller/    # StockController
├── dto/           # StockRequest, StockResponse, StockMovimientoRequest
├── exception/     # GlobalExceptionHandler, StockConflictException, ...
├── model/         # Stock (JPA)
├── repository/    # StockRepository (Spring Data)
└── service/       # StockService (@Transactional)
```

## 🚀 Requisitos

| Requisito | Versión mínima |
|-----------|----------------|
| JDK | **21+** (enforcer) |
| Maven | 3.6.3+ (o wrapper `./mvnw`) |
| Docker + Docker Compose | 24+ |
| Variables de entorno | Obligatorias (ver [Configuración](#-configuración)) |

## ⚙️ Configuración

### Parámetros del servicio

| Parámetro | Valor |
|-----------|-------|
| **Puerto del servicio** | **`8083`** en el ecosistema Calisat (defecto de `CALISAT_INVENTARIO_URL` en ms-carrito/ms-orden); `application.yaml` declara `8080` → arranca con `SERVER_PORT=8083` fuera de Docker |
| Base de datos | PostgreSQL · configurable vía `SPRING_DATASOURCE_URL` |
| `ddl-auto` | `${JPA_DDL_AUTO:update}` (Compose usa `validate` por defecto) |
| Actuator | `health`, `info` |
| Rutas públicas | `/api/v1/public/**`, `/actuator/health` |

### Variables de entorno requeridas

El `application.yaml` **no trae valores por defecto** para datasource ni JWT; son obligatorias (en local o en `docker-compose.yml`):

| Variable | Descripción | Ejemplo / uso en Compose |
|----------|-------------|--------------------------|
| `SPRING_DATASOURCE_URL` | JDBC de PostgreSQL | `jdbc:postgresql://postgres-db:5432/<db>` |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD | `postgres` |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | `default` |
| `JPA_DDL_AUTO` | Estrategia de esquema | `update` (local) · `validate` (Compose) |
| `JWT_ISSUER_URI` | *Issuer* Entra ID | `https://login.microsoftonline.com/<tenant>/v2.0` |
| `JWT_TENANT_ID` | Tenant de Entra ID | id del tenant académico |
| `JWT_AUDIENCE` | *Audience* esperada | client id de la API |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS | origen del API Gateway |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Credenciales del contenedor BD (Compose) | `postgres` / `postgres` |

> ⚠️ **Modo académico**: en un entorno real estas variables deben provenir de un secret manager; los valores de ejemplo son solo para desarrollo.

## ▶️ Ejecución local

### 1. Base de datos

```bash
docker compose up -d postgres-db
```

### 2. Aplicación con variables de entorno

PowerShell (Windows):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/calisat_inventario"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:JWT_ISSUER_URI="https://login.microsoftonline.com/<tenant>/v2.0"
$env:JWT_TENANT_ID="<tenant-id>"
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8083"
```

Bash (Linux/macOS):

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/calisat_inventario"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
export JWT_ISSUER_URI="https://login.microsoftonline.com/<tenant>/v2.0"
export JWT_TENANT_ID="<tenant-id>"
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

### 3. Docker Compose

```bash
docker compose up --build
```

> 📌 El `docker-compose.yml` actual **no publica puertos de la app** en el host (la app queda en la red `calisat-net`). Para exponerla, agrega un mapeo `8083:8080` al servicio `app` o ejecuta en local como se muestra arriba.

## 📡 Endpoints principales

Base: `http://localhost:8083/api/v1/stock`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/v1/stock` | Listar registros de stock (paginado, orden `sku`) | JWT |
| `GET` | `/api/v1/stock/sku/{sku}` | Buscar stock por SKU (404 si no existe) | JWT |
| `GET` | `/api/v1/stock/{id}` | Buscar stock por id (404 si no existe) | JWT |
| `POST` | `/api/v1/stock` | Crear registro (`201` + `Location`; 400 si SKU duplicado) | JWT |
| `POST` | `/api/v1/stock/{sku}/reservar` | Reservar unidades (404 SKU · 409 sin stock libre) | JWT |
| `POST` | `/api/v1/stock/{sku}/liberar` | Liberar reserva (404 SKU · 409 reservado insuficiente) | JWT |
| `POST` | `/api/v1/stock/{sku}/confirmar` | Confirmar salida de stock reservado (404 · 409) | JWT |
| `PUT` | `/api/v1/stock/{id}` | Actualizar SKU y cantidades (404 si no existe) | JWT |
| `DELETE` | `/api/v1/stock/{id}` | Eliminación física del registro (404 si no existe) | JWT |

**Total: 9 endpoints**

### Ejemplo de movimiento

```bash
# Reservar 2 unidades del SKU BARRAS-001
curl -X POST http://localhost:8083/api/v1/stock/BARRAS-001/reservar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cantidad": 2}'
```

## 🗃️ Modelo de datos

### Entidad `Stock` (tabla `stock`)

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| `id` | `Long` | PK, `IDENTITY` |
| `sku` | `String(64)` | **Único**, `NOT NULL` |
| `cantidad_disponible` | `Integer` | `NOT NULL`, `≥ 0` (default `0`) |
| `cantidad_reservada` | `Integer` | `NOT NULL`, `≥ 0` (default `0`) |
| `fecha_actualizacion` | `LocalDateTime` | `@PrePersist` / `@PreUpdate` |

**Reglas de negocio:**

- `reservar(n)` exige `cantidadDisponible − cantidadReservada ≥ n` → `409` en caso contrario.
- `liberar(n)` exige `cantidadReservada ≥ n` → `409` en caso contrario.
- `confirmar(n)` descuenta de disponible y reservada → `409` si no alcanza.
- `actualizar` valida `cantidadReservada ≤ cantidadDisponible`.

## 🔒 Seguridad

- **JWT (OAuth2 Resource Server)** de **Microsoft Entra ID**: validación de *issuer* + *audience* con `DelegatingOAuth2TokenValidator` / `AudienceValidator`.
- **Sin RBAC**: un único nivel autenticado; usuario genérico (*modo académico*).
- **Rutas públicas**: `/api/v1/public/**` y `/actuator/health`; el resto requiere token.
- **CORS** configurable por `CORS_ALLOWED_ORIGINS`; CSRF deshabilitado (API stateless).
- **Sin secretos en el repositorio**: datasource y JWT 100 % por variables de entorno.

## 🧪 Tests

```bash
./mvnw test
```

| Suite | Archivos | Tests |
|-------|----------|-------|
| Unitarios | `StockServiceTest` (18), `StockMovimientoControllerTest` (10), `StockControllerTest` (7), `SecurityConfigTest` (4), `StockResponseTest` (1), `DominioStockGuardTest` (1) | **41** |

## 📦 Despliegue

### Docker

```bash
docker build -t calisat-ms-inventario:1.2.0 .
docker run -p 8083:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://<host-db>:5432/calisat_inventario" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="postgres" \
  -e JWT_ISSUER_URI="https://login.microsoftonline.com/<tenant>/v2.0" \
  -e JWT_TENANT_ID="<tenant-id>" \
  --name calisat-ms-inventario calisat-ms-inventario:1.2.0
```

**Dockerfile multi-stage:**

1. `maven` (Temurin 21) → `mvn clean package -DskipTests`.
2. `eclipse-temurin:21-jre-alpine` → JAR con usuario `spring`, `MaxRAMPercentage=75`, `HEALTHCHECK` en `/actuator/health`.

### Docker Compose

```bash
docker compose up --build
```

Incluye PostgreSQL 15 con health check y volumen `calisat_inventario_data` en la red `calisat-net`.

## 🔗 Microservicios relacionados

| Repositorio | Relación |
|-------------|----------|
| [calisat-ms-carrito](https://github.com/DavNat13/calisat-ms-carrito) | Lee stock por SKU vía `InventarioClient` (`CALISAT_INVENTARIO_URL`, defecto `:8083`) |
| [calisat-ms-orden](https://github.com/DavNat13/calisat-ms-orden) | Reserva/libera/confirma stock en el saga de la orden (`CALISAT_INVENTARIO_URL`, defecto `:8083`) |
| [calisat-ms-catalogo](https://github.com/DavNat13/calisat-ms-catalogo) | Catálogo de productos (puerto 8082) |
| [calisat-ms-usuarios](https://github.com/DavNat13/calisat-ms-usuarios) | Perfil y direcciones (puerto 8081) |
| [calisat-ms-envios](https://github.com/DavNat13/calisat-ms-envios) | Envíos y seguimiento (puerto 8086) |
| [calisat-ms-notificaciones](https://github.com/DavNat13/calisat-ms-notificaciones) | Notificaciones (puerto 8087) |
| [calisat-frontend](https://github.com/DavNat13/calisat-frontend) | SPA React 19 (v1.4.0) |

## 📄 Licencia y modo académico

Proyecto desarrollado en **modo académico**; sin licencia open source formal. Credenciales de ejemplo y configuración JWT son solo para fines educativos.

- **Versión actual**: `1.2.0`
- **Historial de cambios**: [`CHANGELOG.md`](CHANGELOG.md)
- **Plan de trabajo**: [`PLAN.md`](PLAN.md) (fases de dominio, compose y CHANGELOG)
