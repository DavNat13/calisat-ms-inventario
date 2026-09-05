package com.califorge.msinventario.service;

import com.califorge.msinventario.dto.StockRequest;
import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.model.Stock;
import com.califorge.msinventario.repository.StockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Crea un registro de stock validando que el SKU sea unico.
     *
     * Nota sobre concurrencia: el check {@code existsBySku} es una defensa temprana
     * para responder 400 rapido, NO una garantia de unicidad. La garantia real la pone
     * la constraint UNIQUE del SKU en BD (DataIntegrityViolationException -> 400 en el
     * GlobalExceptionHandler) en escenarios TOCTOU con requests simultaneos.
     */
    public Stock crear(StockRequest request) {
        if (stockRepository.existsBySku(request.sku())) {
            throw new SkuDuplicadoException(request.sku());
        }
        return stockRepository.save(mapearParaGrabar(request));
    }

    @Transactional(readOnly = true)
    public List<Stock> listar() {
        return stockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Stock> listar(Pageable pageable) {
        return stockRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Stock> buscarPorId(Long id) {
        return stockRepository.findById(id);
    }

    /**
     * Actualiza sku y cantidades del stock. Semantica REPLACE: el cliente envia el
     * nuevo saldo de cantidadDisponible y cantidadReservada. Mover stock entre ambos
     * campos equivale a declarar el saldo destino de cada uno.
     */
    public Optional<Stock> actualizar(Long id, StockRequest request) {
        if (request.sku() != null && stockRepository.existsBySku(request.sku())) {
            Optional<Stock> mismo = stockRepository.findBySku(request.sku());
            if (mismo.isEmpty() || !mismo.get().getId().equals(id)) {
                throw new SkuDuplicadoException(request.sku());
            }
        }
        return stockRepository.findById(id)
                .map(stock -> {
                    stock.setSku(request.sku());
                    stock.setCantidadDisponible(request.cantidadDisponible());
                    stock.setCantidadReservada(request.cantidadReservada());
                    return stockRepository.save(stock);
                });
    }

    /**
     * Eliminacion fisica del registro de stock (sin baja logica: el dominio no tiene 'activo').
     */
    public Optional<Stock> eliminar(Long id) {
        return stockRepository.findById(id)
                .map(stock -> {
                    stockRepository.delete(stock);
                    return stock;
                });
    }

    private Stock mapearParaGrabar(StockRequest request) {
        Stock stock = new Stock();
        stock.setSku(request.sku());
        stock.setCantidadDisponible(request.cantidadDisponible());
        stock.setCantidadReservada(request.cantidadReservada());
        return stock;
    }
}