# Plan de Trabajo — calisat-ms-inventario

**Proyecto:** Calisat · **Última actualización:** 30-08-2026 · **Modo:** Por fases

## Contexto
Microservicio de inventario de **productos deportivos de calistenia en venta**. La base (Spring Boot 4.1.0, Java 21, JWT Azure AD, Docker/Compose, health check) ya existe como scaffold; el plan construye la **lógica de negocio** replicando el patrón de `calisat-ms-usuarios` (`model` + `repository` + `service` + `controller`).

**Arquitectura:** Frontend → Azure AD (login MS) → JWT → API Gateway → EC2:8080 → Spring Boot · DB PostgreSQL 15 en contenedor · Endpoints `/api/v1/inventario/productos` · Puerto 8080 · Red `calisat-net` · Health `/actuator/health`.

## Entregables
| Fase | Entregable | Criterio de salida |
|---|---|---|
| 0 | Validación base actual | Compila sin errores |
| 1 | Entidad `Producto` (model) | Mapeo JPA correcto |
| 2 | Repositorio `ProductoRepository` | Consultas derivadas definidas |
| 3 | Servicio `ProductoService` | CRUD + reglas de negocio |
| 4 | Controlador `ProductoController` | Endpoints `/api/v1/inventario/productos` |
| 5 | Alineación docker-compose a doc | Red `calisat-net` + puerto 8080 |
| 6 | `CHANGELOG.md` en repo | Historial de versión |
| 7 | Verificación final | Compila y cumple convenciones |

---

# Fase 0 — Validación de la base
**Objetivo:** Confirmar que el scaffold heredado compila y arranca el contexto antes de introducir el dominio.
- Revisar `pom.xml` (dependencias, Boot 4.1.0, Java 21), `SecurityConfig` (JWT Azure AD, CORS, `permitAll`), `application.yaml` (env obligatorias, datasource, JPA), `Dockerfile` y `docker-compose.yml`.
- Ejecutar `./mvnw.cmd compile` y confirmar **BUILD SUCCESS**.
- **Salida:** compilación OK; sin endpoints aún (Fase 4).

# Fase 1 — Entidad `Producto`
**Objetivo:** Entidad JPA de un producto deportivo en venta.
- Crear `src/main/java/com/califorge/msinventario/model/Producto.java`.
- `@Entity @Table(name = "producto")`; `@Id UUID` con `@GeneratedValue(generator="UUID")` + `@GenericGenerator(name="UUID", strategy="org.hibernate.id.UUIDGenerator")` (mismo patrón que `UsuarioProfile`).
- Campos (ver tabla más abajo), constructores (vacío y mínimo), getters/setters y `@PrePersist` para `fechaRegistro`.

| Campo | Tipo | Reglas |
|---|---|---|
| `sku` | String | `@NotBlank`, unique, nullable=false, length 64 |
| `nombre` | String | `@NotBlank`, nullable=false, length 255 |
| `categoria` | String | opcional, length 100 (Barras/Paralelas/Anillas/Accesorios) |
| `descripcion` | String | opcional, length 1000 |
| `precio` | BigDecimal | importe de venta |
| `stock` | Integer | default 0 |
| `unidadMedida` | String | opcional, length 50 (unidad/par/set) |
| `activo` | Boolean | default true (baja lógica) |
| `fechaRegistro` | LocalDateTime | `@PrePersist` = now |

- **Salida:** mapeo JPA correcto y consistente con `UsuarioProfile`.

# Fase 2 — Repositorio `ProductoRepository`
**Objetivo:** Capa de acceso a datos de `Producto`.
- Crear `src/main/java/com/califorge/msinventario/repository/ProductoRepository.java`.
- `@Repository extends JpaRepository<Producto, UUID>`.
- Métodos derivados: `Optional<Producto> findBySku(String sku)`, `boolean existsBySku(String sku)`, `List<Producto> findByActivoTrue()`.
- **Salida:** consultas correctas y fuente única de validación de SKU único.

# Fase 3 — Servicio `ProductoService`
**Objetivo:** Lógica de negocio del inventario.
- Crear `src/main/java/com/califorge/msinventario/service/ProductoService.java`.
- `@Service` + `@Transactional`; inyección por constructor de `ProductoRepository`.
- `crear(Producto)` — valida `existsBySku`; si existe lanza excepción de negocio (SKU duplicado).
- `listar()` — solo activos (`findByActivoTrue`). `buscarPorId(UUID)` — `Optional<Producto>`.
- `actualizar(UUID, ...)` — actualiza campos editables. `desactivar(UUID)` — baja lógica (`activo=false`).
- Lecturas con `@Transactional(readOnly = true)`; retorna `Optional` para permitir 404 en controlador.
- **Reglas:** SKU único; `DELETE` = baja lógica (no físico); listar solo activos.
- **Salida:** CRUD + validación implementada, sin dependencia del framework HTTP.

# Fase 4 — Controlador `ProductoController`
**Objetivo:** Exponer endpoints REST.
- Crear `src/main/java/com/califorge/msinventario/controller/ProductoController.java`.
- `@RestController` + `@RequestMapping("/api/v1/inventario/productos")`.

| Método | Ruta | Acción |
|---|---|---|
| GET | `/` | Listar activos |
| GET | `/{id}` | Buscar (404 si no existe) |
| POST | `/` | Crear (`@Valid`); 400 si SKU duplicado |
| PUT | `/{id}` | Actualizar (404 si no existe) |
| DELETE | `/{id}` | Desactivar (baja lógica); 404 si no existe |

- Respuestas `ResponseEntity<Map<String,Object>>` (estilo `UsuarioController`); errores 400/404; Javadoc breve.
- **Salida:** endpoints funcionales y consistentes con `calisat-ms-usuarios`.

# Fase 5 — Alineación `docker-compose.yml`
**Objetivo:** Alinear compose local con la doc `02`/`03`.
- Cambiar red `calisat-inventario-net` → **`calisat-net`**.
- Cambiar puertos `8082:8080` → **`8080:8080`**.
- Mantener servicio `postgres` con su healthcheck, volumen `calisat_inventario_data` y `depends_on service_healthy`.
- **Nota (opcional, no bloquea):** el `Dockerfile` no incluye `HEALTHCHECK` (con `wget /actuator/health`) que propone la doc `03`; puede sumarse en una fase de infra posterior.
- **Salida:** compose coherente con la doc (red `calisat-net`, puerto 8080).

# Fase 6 — `CHANGELOG.md`
**Objetivo:** Registrar historial del microservicio en el repo (formato `calisat-ms-usuarios`).
- Crear `CHANGELOG.md` en la raíz del repo con formato `## [x.y.z] - fecha` + `### Added`/`### Fixed`.
- Registrar: base actual (Boot 4.1.0, Docker, JWT Azure AD, health), primera iteración de dominio (Producto + repo + service + controller) y ajuste de compose.
- **Salida:** `CHANGELOG.md` presente con historial claro.

# Fase 7 — Verificación final
- `./mvnw.cmd compile` → **BUILD SUCCESS** sin warnings relevantes.
- Revisar consistencia de paquetes/nombres con `calisat-ms-usuarios` y estilo de endpoints.
- Verificar que no se exponen secretos/credenciales y que `/actuator/health` y rutas de seguridad son correctas.
- **Salida:** compilación verificada y código alineado al patrón del proyecto.

---

## Fuera de alcance (iteraciones futuras)
- **Movimientos/stock transaccional** (entradas, salidas, ajustes) — `stock` queda como valor simple.
- **Categorías como entidad** (aquí `categoria` es String simple) y **almacenes/ubicaciones**.
- **Migraciones de esquema** (se mantiene `ddl-auto: update`).
- **`application-prod.yaml`** y **`.env.example`** (descartados por decisión del equipo).
- **Infraestructura AWS real** (EC2, API Gateway, Secrets Manager/SSM) — fuera del repo.
- **`HEALTHCHECK` del Dockerfile** — opcional (documentado en Fase 5).

## Convenciones
- Capas `model`+`repository`+`service`+`controller` (idéntico a `calisat-ms-usuarios`).
- IDs `UUID` con `@GenericGenerator(name="UUID", strategy="org.hibernate.id.UUIDGenerator")`.
- Respuestas `ResponseEntity<Map<String, Object>>` construidas a mano.
- Sin credenciales en código: config sensible vía `${VARIABLE}` sin fallback salvo justificado.
- Baja lógica para desactivar productos (`activo=false`).
