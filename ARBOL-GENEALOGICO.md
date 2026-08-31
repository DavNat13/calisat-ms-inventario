# Árbol genealógico — Calisat ms-inventario

**Proyecto:** Calisat · **Última actualización:** 30-08-2026

Estructura de los archivos que se crearán según `PLAN.md`. Indica la fase de origen, el paquete/raíz, y su vínculo de dependencia (qué archivo alimenta a cuál).

```
calisat-ms-inventario/
│
│  ┌────────────────────────────────────────────────────────────────────┐
│  │  EXISTENTES (base, no se modifican salvo Fase 5 y 6)               │
│  ├────────────────────────────────────────────────────────────────────┤
│  ├── pom.xml                                  (Fase 0) revisar
│  ├── Dockerfile                               (Fase 0) revisar
│  ├── docker-compose.yml                       (Fase 5) MODIFICAR  ← red + puerto
│  ├── CHANGELOG.md                             (Fase 6) CREAR  (no existe aún)
│  ├── PLAN.md                                  (este plan)
│  └── src/main/
│      ├── resources/application.yaml           (Fase 0) revisar
│      └── java/com/califorge/msinventario/
│          ├── CalisatMsInventarioApplication.java
│          └── config/SecurityConfig.java
│
│  ┌────────────────────────────────────────────────────────────────────┐
│  │  NUEVOS — Capa de DOMINIO (patrón: calisat-ms-usuarios)           │
│  └────────────────────────────────────────────────────────────────────┘
│
└── src/main/java/com/califorge/msinventario/
    │
    ├── model/                                   ──► Fase 1
    │   └── Producto.java                        (ENTIDAD JPA raíz del dominio)
    │         │
    │         │  referencia a
    │         ▼
    ├── repository/                              ──► Fase 2
    │   └── ProductoRepository.java              (usa Producto, PK UUID)
    │         │
    │         │  inyectado en
    │         ▼
    ├── service/                                 ──► Fase 3
    │   └── ProductoService.java                 (usa ProductoRepository + Producto)
    │         │
    │         │  usado por
    │         ▼
    └── controller/                              ──► Fase 4
        └── ProductoController.java              (usa ProductoService)
```

## Dependencias entre archivos (genealogía funcional)

```
Producto.java  ──entidad──►  ProductoRepository.java  ──inyecta──►  ProductoService.java  ──expone──►  ProductoController.java
```

| Archivo (nuevo) | Fase | Depende de | Dependientes |
|---|---|---|---|
| `model/Producto.java` | 1 | — (raíz) | `ProductoRepository`, `ProductoService` |
| `repository/ProductoRepository.java` | 2 | `model/Producto` | `ProductoService` |
| `service/ProductoService.java` | 3 | `repository/ProductoRepository`, `model/Producto` | `ProductoController` |
| `controller/ProductoController.java` | 4 | `service/ProductoService` | — (expone API REST) |

## Archivos de infraestructura / documentación

| Archivo | Fase | Tipo de cambio |
|---|---|---|
| `docker-compose.yml` | 5 | Modificación (red `calisat-net`, puerto 8080) |
| `CHANGELOG.md` | 6 | Creación (historial de versión) |

## Ruta absoluta de los archivos nuevos

```
C:\Users\Sasuk\DUOC\Tercer año 2026\Cloud Native I\CaliSat\calisat-ms-inventario\
└── src\main\java\com\califorge\msinventario\
    ├── model\Producto.java
    ├── repository\ProductoRepository.java
    ├── service\ProductoService.java
    └── controller\ProductoController.java
```
