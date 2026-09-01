package com.califorge.msinventario.controller;

import com.califorge.msinventario.dto.ProductoRequest;
import com.califorge.msinventario.dto.ProductoResponse;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventario")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /inventario
     * Lista los productos activos.
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {
        List<ProductoResponse> productos = productoService.listar().stream()
                .map(ProductoResponse::desde)
                .toList();
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /inventario/{id}
     * Busca un producto por id. Devuelve 404 si no existe o esta inactivo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable UUID id) {
        return productoService.buscarPorId(id)
                .map(ProductoResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /inventario
     * Crea un producto. Devuelve 400 si el SKU ya existe, 201 en éxito con Location.
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        Producto guardado = productoService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProductoResponse.desde(guardado));
    }

    /**
     * PUT /inventario/{id}
     * Actualiza un producto. Devuelve 404 si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request)
                .map(ProductoResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /inventario/{id}
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
}
