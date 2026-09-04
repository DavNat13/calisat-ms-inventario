package com.califorge.msinventario.controller;

import com.califorge.msinventario.dto.StockRequest;
import com.califorge.msinventario.model.Stock;
import com.califorge.msinventario.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba unitaria del StockController usando Mockito para el servicio y MockMvc
 * standalone (en Spring Boot 4.x no existe @WebMvcTest; se usa standaloneSetup).
 */
@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController).build();
    }

    @Test
    void listar_devuelveListaDeStocks() throws Exception {
        Stock stock = stock(1L, "SKU-1", 10, 0);
        when(stockService.listar()).thenReturn(List.of(stock));

        mockMvc.perform(get("/api/v1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("SKU-1")))
                .andExpect(jsonPath("$[0].cantidadDisponible", is(10)))
                .andExpect(jsonPath("$[0].cantidadReservada", is(0)));
    }

    @Test
    void buscarPorId_devuelveStock() throws Exception {
        Stock stock = stock(1L, "SKU-1", 10, 0);
        when(stockService.buscarPorId(1L)).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/v1/stock/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sku", is("SKU-1")));
    }

    @Test
    void buscarPorId_devuelve404SiNoExiste() throws Exception {
        when(stockService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stock/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_devuelve201ConLocation() throws Exception {
        Stock guardado = stock(1L, "SKU-1", 10, 2);
        when(stockService.crear(any(StockRequest.class))).thenReturn(guardado);

        String body = """
                {"sku":"SKU-1","cantidadDisponible":10,"cantidadReservada":2}
                """;

        mockMvc.perform(post("/api/v1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/v1/stock/1")))
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.cantidadDisponible", is(10)))
                .andExpect(jsonPath("$.cantidadReservada", is(2)));
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        Stock actualizado = stock(1L, "SKU-2", 4, 6);
        when(stockService.actualizar(any(Long.class), any(StockRequest.class)))
                .thenReturn(Optional.of(actualizado));

        String body = """
                {"sku":"SKU-2","cantidadDisponible":4,"cantidadReservada":6}
                """;

        mockMvc.perform(put("/api/v1/stock/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-2")))
                .andExpect(jsonPath("$.cantidadDisponible", is(4)))
                .andExpect(jsonPath("$.cantidadReservada", is(6)));
    }

    @Test
    void eliminar_devuelve200() throws Exception {
        Stock eliminado = stock(1L, "SKU-1", 10, 0);
        when(stockService.eliminar(1L)).thenReturn(Optional.of(eliminado));

        mockMvc.perform(delete("/api/v1/stock/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Stock eliminado correctamente")));
    }

    @Test
    void eliminar_devuelve404SiNoExiste() throws Exception {
        when(stockService.eliminar(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/stock/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    private Stock stock(Long id, String sku, int disponible, int reservada) {
        Stock stock = new Stock(sku, disponible, reservada);
        stock.setId(id);
        stock.setFechaActualizacion(LocalDateTime.of(2026, 9, 4, 10, 30));
        return stock;
    }
}