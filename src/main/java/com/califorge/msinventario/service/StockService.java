package com.califorge.msinventario.service;

import com.califorge.msinventario.dto.StockMovimientoRequest;
import com.califorge.msinventario.dto.StockRequest;
import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.exception.StockConflictException;
import com.califorge.msinventario.exception.StockInvalidoException;
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

    @Transactional(readOnly = true)
    public Optional<Stock> buscarPorSku(String sku) {
        return stockRepository.findBySku(sku);
    }

    /**
     * Reserva stock: requiere que haya suficiente stock libre
     * (disponible - reservada) para la cantidad pedida.
     */
    public Optional<Stock> reservar(String sku, StockMovimientoRequest request) {
        return stockRepository.findBySku(sku).map(stock -> {
            int libre = stock.getCantidadDisponible() - stock.getCantidadReservada();
            if (libre < request.cantidad()) {
                throw new StockConflictException("Stock insuficiente");
            }
            stock.setCantidadReservada(stock.getCantidadReservada() + request.cantidad());
            return stockRepository.save(stock);
        });
    }

    /**
     * Libera stock reservado: requiere que la cantidad reservada actual
     * sea mayor o igual que la cantidad a liberar.
     */
    public Optional<Stock> liberar(String sku, StockMovimientoRequest request) {
        return stockRepository.findBySku(sku).map(stock -> {
            if (stock.getCantidadReservada() < request.cantidad()) {
                throw new StockConflictException("No hay suficiente stock reservado");
            }
            stock.setCantidadReservada(stock.getCantidadReservada() - request.cantidad());
            return stockRepository.save(stock);
        });
    }

    /**
     * Confirma la salida de stock reservado: descuenta de disponible y de reservado.
     * Exige que haya suficiente stock reservado y disponible.
     */
    public Optional<Stock> confirmar(String sku, StockMovimientoRequest request) {
        return stockRepository.findBySku(sku).map(stock -> {
            if (stock.getCantidadReservada() < request.cantidad()) {
                throw new StockConflictException("No hay suficiente stock reservado");
            }
            if (stock.getCantidadDisponible() < request.cantidad()) {
                throw new StockConflictException("Stock insuficiente");
            }
            stock.setCantidadDisponible(stock.getCantidadDisponible() - request.cantidad());
            stock.setCantidadReservada(stock.getCantidadReservada() - request.cantidad());
            return stockRepository.save(stock);
        });
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
        if (request.cantidadReservada() != null && request.cantidadDisponible() != null
                && request.cantidadReservada() > request.cantidadDisponible()) {
            throw new StockInvalidoException(
                    "cantidadReservada no puede ser mayor que cantidadDisponible");
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