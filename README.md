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

Se expone bajo `/api/v1/stock` con **PostgreSQL** como almacén, validación de **JWT (Microsoft Entra ID)** y RBAC (escrituras solo `ADMINISTRADOR`). Las claves de integración (`ms-carrito`, `ms-orden`) lo consumen en el puerto **8083**.

## ✨ Características principales

- 📦 **Inventario por SKU**: registro único por SKU con cantidades disponible/reservada.
- 🔁 **Movimientos con control de conflictos**: `reservar`, `liberar` y `confirmar` devuelven `404` (SKU inexistente) o `409` (stock insuficiente / reservado insuficiente).
- ✅ **Validaciones de dominio**: cantidades no negativas; `cantidadReservada ≤ cantidadDisponible`.
- 🔎 **Consultas múltiples**: listado paginado, por `id` y por `sku` (búsqueda canónica).
- 🧾 **Manejo global de errores**: `GlobalExceptionHandler` con `SkuDuplicadoException`, `StockConflictException` (409) y `StockInvalidoException` (400).
- 🩺 **Actuator**: `health` e `info` con detalle cuando está autorizado.
- ⚙️ **Configuración 100 % expuesta en el repo**: datasource, JWT (Entra ID) y CORS en valores literales de `application.yaml` y `SecurityConfig`, sin variables de entorno.
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
| Variables de entorno | **Ninguna** (configuración literal en el repo) |

## ⚙️ Configuración

### Parámetros del servicio

| Parámetro | Valor |
|-----------|-------|
| **Puerto del servicio** | **`8083`** en el ecosistema Calisat (defecto de `CALISAT_INVENTARIO_URL` en ms-carrito/ms-orden); `application.yaml` declara `8080` → fuera de Docker arranca con `--server.port=8083` (compose publica `8083:8080`) |
| Base de datos | PostgreSQL · `jdbc:postgresql://postgres:5432/calisat_inventario` (literal en `application.yaml`; servicio `postgres` del compose) |
| `ddl-auto` | `update` (literal) |
| Actuator | `health`, `info` |
| Rutas públicas | `/api/v1/public/**`, `/actuator/health` |

### Configuración expuesta (sin variables de entorno)

Toda la configuración está **en el repositorio**, igual que el resto de microservicios Calisat:

| Parámetro | Valor literal | Ubicación |
|-----------|---------------|-----------|
| Datasource | `jdbc:postgresql://postgres:5432/calisat_inventario` · `postgres` / `postgres` | `application.yaml` |
| `ddl-auto` | `update` | `application.yaml` |
| *Issuer* Entra ID | `https://login.microsoftonline.com/e5372bf0-c5e3-4286-887c-79069f209c1f/v2.0` | `application.yaml` + `SecurityConfig.ISSUER_URI` |
| *Audience* esperada | `d221f0d2-1a7c-4872-ad6c-367a1f0717ec` | `application.yaml` + `SecurityConfig.EXPECTED_AUDIENCE` |
| Origen CORS | `https://ezeh839whh.execute-api.us-east-1.amazonaws.com` | `SecurityConfig.ALLOWED_ORIGIN` |
| Credenciales BD (contenedor) | `calisat_inventario` / `postgres` / `postgres` | `docker-compose.yml` |

## ▶️ Ejecución local

### 1. Base de datos

```bash
docker compose up -d postgres
```

### 2. Aplicación

PowerShell (Windows):

```powershell
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8083"
```

Bash (Linux/macOS):

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

> 💡 Sin variables de entorno: la configuración (datasource, JWT y CORS) está literal en `application.yaml` y `SecurityConfig`.

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
- **RBAC**: claim `roles` del JWT → `ROLE_*`; escrituras de stock (`POST`/`PUT`/`DELETE`) solo para `ADMINISTRADOR`.
- **Rutas públicas**: `/api/v1/public/**` y `/actuator/health`; el resto requiere token.
- **CORS** con origen literal `https://ezeh839whh.execute-api.us-east-1.amazonaws.com` (`SecurityConfig.ALLOWED_ORIGIN`); CSRF deshabilitado (API stateless).
- **Configuración expuesta en el repo**: datasource, JWT y CORS literales; sin variables de entorno.

## 🧪 Tests

```bash
./mvnw test
```

| Suite | Archivos | Tests |
|-------|----------|-------|
| Unitarios | `StockServiceTest` (18), `StockMovimientoControllerTest` (10), `StockControllerTest` (7), `SecurityConfigTest` (3), `StockResponseTest` (1), `DominioStockGuardTest` (1) | **40** |

## 📦 Despliegue

### Docker

```bash
docker build -t calisat-ms-inventario:1.3.1 .
docker run -p 8083:8080 --name calisat-ms-inventario calisat-ms-inventario:1.3.1
```

> Sin `-e`: no se requiere ninguna variable de entorno (configuración literal en el JAR). Para otro host de BD basta editar el literal de `application.yaml` antes del build.

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

- **Versión actual**: `1.3.1`
- **Historial de cambios**: [`CHANGELOG.md`](CHANGELOG.md)
- **Plan de trabajo**: [`PLAN.md`](PLAN.md) (fases de dominio, compose y CHANGELOG)
