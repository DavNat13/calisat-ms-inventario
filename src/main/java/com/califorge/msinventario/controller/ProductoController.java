package com.califorge.msinventario.controller;

import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.model.Producto;
import com.califorge.msinventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventario/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /api/v1/inventario/productos
     * Lista los productos activos.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar() {
        List<Map<String, Object>> productos = productoService.listar().stream()
                .map(this::aMapa)
                .toList();
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/v1/inventario/productos/{id}
     * Busca un producto por id. Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable UUID id) {
        return productoService.buscarPorId(id)
                .map(producto -> ResponseEntity.ok(aMapa(producto)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/inventario/productos
     * Crea un producto. Devuelve 400 si el SKU ya existe.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody Producto producto) {
        try {
            Producto guardado = productoService.crear(producto);
            return ResponseEntity.ok(aMapa(guardado));
        } catch (SkuDuplicadoException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * PUT /api/v1/inventario/productos/{id}
     * Actualiza un producto. Devuelve 404 si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable UUID id,
            @RequestBody Producto producto) {
        try {
            return productoService.actualizar(
                            id,
                            producto.getSku(),
                            producto.getNombre(),
                            producto.getCategoria(),
                            producto.getDescripcion(),
                            producto.getPrecio(),
                            producto.getStock(),
                            producto.getUnidadMedida())
                    .map(p -> ResponseEntity.ok(aMapa(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SkuDuplicadoException ex) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", ex.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/inventario/productos/{id}
     * Desactiva un producto (baja logica). Devuelve 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> desactivar(@PathVariable UUID id) {
        return productoService.desactivar(id)
                .map(p -> ResponseEntity.ok(Map.<String, Object>of(
                        "id", p.getId().toString(),
                        "mensaje", "Producto desactivado correctamente"
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> aMapa(Producto p) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", p.getId().toString());
        mapa.put("sku", p.getSku());
        mapa.put("nombre", p.getNombre());
        mapa.put("categoria", p.getCategoria());
        mapa.put("descripcion", p.getDescripcion());
        mapa.put("precio", p.getPrecio());
        mapa.put("stock", p.getStock());
        mapa.put("unidadMedida", p.getUnidadMedida());
        mapa.put("activo", p.getActivo());
        mapa.put("fechaRegistro", p.getFechaRegistro() != null ? p.getFechaRegistro().toString() : null);
        return mapa;
    }
}
