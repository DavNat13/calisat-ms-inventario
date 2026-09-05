package com.califorge.msinventario.controller;

import com.califorge.msinventario.dto.StockRequest;
import com.califorge.msinventario.dto.StockResponse;
import com.califorge.msinventario.model.Stock;
import com.califorge.msinventario.service.StockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * GET /api/v1/stock
     * Lista registros de stock con paginacion.
     */
    @GetMapping
    public ResponseEntity<Page<StockResponse>> listar(
            @PageableDefault(size = 20, sort = "sku") Pageable pageable) {
        Page<StockResponse> stocks = stockService.listar(pageable)
                .map(StockResponse::desde);
        return ResponseEntity.ok(stocks);
    }

    /**
     * GET /api/v1/stock/{id}
     * Busca un registro de stock por id. Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> buscarPorId(@PathVariable Long id) {
        return stockService.buscarPorId(id)
                .map(StockResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/stock
     * Crea un registro de stock. Devuelve 400 si el SKU ya existe, 201 en exito con Location.
     */
    @PostMapping
    public ResponseEntity<StockResponse> crear(@Valid @RequestBody StockRequest request) {
        Stock guardado = stockService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();
        return ResponseEntity.created(location).body(StockResponse.desde(guardado));
    }

    /**
     * PUT /api/v1/stock/{id}
     * Actualiza sku y cantidades. Devuelve 404 si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody StockRequest request) {
        return stockService.actualizar(id, request)
                .map(StockResponse::desde)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/stock/{id}
     * Elimina fisicamente el registro de stock. Devuelve 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        return stockService.eliminar(id)
                .map(s -> ResponseEntity.ok(Map.<String, Object>of(
                        "id", s.getId(),
                        "mensaje", "Stock eliminado correctamente"
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
