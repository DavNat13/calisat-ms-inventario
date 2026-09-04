package com.califorge.msinventario.dto;

import com.califorge.msinventario.model.Stock;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockResponseTest {

    @Test
    void desde_mapeaTodosLosCamposDeLaEntidad() {
        Stock stock = new Stock("SKU-1", 10, 2);
        stock.setId(1L);
        stock.setFechaActualizacion(LocalDateTime.of(2026, 9, 4, 10, 30));

        StockResponse response = StockResponse.desde(stock);

        assertEquals(1L, response.id());
        assertEquals("SKU-1", response.sku());
        assertEquals(10, response.cantidadDisponible());
        assertEquals(2, response.cantidadReservada());
        assertNotNull(response.fechaActualizacion());
    }
}