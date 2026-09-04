package com.califorge.msinventario.service;

import com.califorge.msinventario.dto.StockRequest;
import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.model.Stock;
import com.califorge.msinventario.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void crear_mapeaCamposDeStock() {
        when(stockRepository.existsBySku("SKU-1")).thenReturn(false);
        Stock guardado = new Stock();
        guardado.setId(1L);
        when(stockRepository.save(any(Stock.class))).thenReturn(guardado);

        StockRequest request = new StockRequest("SKU-1", 10, 2);

        stockService.crear(request);

        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(captor.capture());
        Stock entidad = captor.getValue();
        assertEquals("SKU-1", entidad.getSku());
        assertEquals(10, entidad.getCantidadDisponible());
        assertEquals(2, entidad.getCantidadReservada());
    }

    @Test
    void crear_lanzaSkuDuplicado() {
        when(stockRepository.existsBySku("SKU-1")).thenReturn(true);
        StockRequest request = new StockRequest("SKU-1", 10, 0);

        assertThrows(SkuDuplicadoException.class, () -> stockService.crear(request));
        verify(stockRepository).existsBySku("SKU-1");
    }

    @Test
    void listar_devuelveTodosLosStocks() {
        Stock stock = stock("SKU-1", 10, 0);
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        List<Stock> resultado = stockService.listar();

        assertEquals(1, resultado.size());
        assertEquals("SKU-1", resultado.get(0).getSku());
        verify(stockRepository).findAll();
    }

    @Test
    void buscarPorId_devuelveStock() {
        Stock stock = stock("SKU-1", 10, 0);
        stock.setId(1L);
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        Optional<Stock> resultado = stockService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("SKU-1", resultado.get().getSku());
    }

    @Test
    void buscarPorId_devuelveVacioSiNoExiste() {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Stock> resultado = stockService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void actualizar_reescribeCantidades() {
        Stock existente = stock("SKU-1", 10, 0);
        existente.setId(1L);
        when(stockRepository.existsBySku("SKU-2")).thenReturn(false);
        when(stockRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(stockRepository.save(any(Stock.class))).thenReturn(existente);

        StockRequest request = new StockRequest("SKU-2", 4, 6);

        Optional<Stock> resultado = stockService.actualizar(1L, request);

        assertTrue(resultado.isPresent());
        assertEquals("SKU-2", resultado.get().getSku());
        assertEquals(4, resultado.get().getCantidadDisponible());
        assertEquals(6, resultado.get().getCantidadReservada());
    }

    @Test
    void actualizar_lanzaSkuDuplicadoSiElSkuEsDeOtroStock() {
        Stock otro = stock("SKU-2", 3, 0);
        otro.setId(2L);
        when(stockRepository.existsBySku("SKU-2")).thenReturn(true);
        when(stockRepository.findBySku("SKU-2")).thenReturn(Optional.of(otro));

        StockRequest request = new StockRequest("SKU-2", 4, 6);

        assertThrows(SkuDuplicadoException.class, () -> stockService.actualizar(1L, request));
    }

    @Test
    void actualizar_devuelveVacioSiNoExiste() {
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        StockRequest request = new StockRequest("SKU-1", 1, 0);

        Optional<Stock> resultado = stockService.actualizar(99L, request);

        assertFalse(resultado.isPresent());
    }

    @Test
    void eliminar_borraElStock() {
        Stock stock = stock("SKU-1", 10, 0);
        stock.setId(1L);
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        Optional<Stock> resultado = stockService.eliminar(1L);

        assertTrue(resultado.isPresent());
        verify(stockRepository).delete(stock);
    }

    @Test
    void eliminar_devuelveVacioSiNoExiste() {
        when(stockRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Stock> resultado = stockService.eliminar(1L);

        assertFalse(resultado.isPresent());
    }

    private Stock stock(String sku, int disponible, int reservada) {
        Stock stock = new Stock(sku, disponible, reservada);
        stock.setFechaActualizacion(LocalDateTime.of(2026, 9, 4, 10, 30));
        return stock;
    }
}