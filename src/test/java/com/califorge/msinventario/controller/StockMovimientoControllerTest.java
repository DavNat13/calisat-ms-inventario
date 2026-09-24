package com.califorge.msinventario.controller;

import com.califorge.msinventario.exception.GlobalExceptionHandler;
import com.califorge.msinventario.exception.StockConflictException;
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
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas MockMvc standalone de los endpoints de consulta por SKU y de
 * movimientos de stock (reservar, liberar, confirmar).
 */
@ExtendWith(MockitoExtension.class)
class StockMovimientoControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void buscarPorSku_devuelve200() throws Exception {
        Stock stock = stock("SKU-1", 10, 2);
        when(stockService.buscarPorSku("SKU-1")).thenReturn(Optional.of(stock));

        mockMvc.perform(get("/api/v1/stock/sku/{sku}", "SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.cantidadDisponible", is(10)))
                .andExpect(jsonPath("$.cantidadReservada", is(2)));
    }

    @Test
    void buscarPorSku_devuelve404SiNoExiste() throws Exception {
        when(stockService.buscarPorSku("NO-EXISTE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/stock/sku/{sku}", "NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservar_devuelve200() throws Exception {
        Stock reservado = stock("SKU-1", 10, 5);
        when(stockService.reservar(eq("SKU-1"), any()))
                .thenReturn(Optional.of(reservado));

        String body = """
                {"cantidad": 3, "refOrden": "ord-1"}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/reservar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.cantidadReservada", is(5)));
    }

    @Test
    void reservar_devuelve409StockInsuficiente() throws Exception {
        when(stockService.reservar(eq("SKU-1"), any()))
                .thenThrow(new StockConflictException("Stock insuficiente"));

        String body = """
                {"cantidad": 100}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/reservar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", is("Stock insuficiente")));
    }

    @Test
    void reservar_devuelve404SiNoExisteElSku() throws Exception {
        when(stockService.reservar(eq("NO-EXISTE"), any()))
                .thenReturn(Optional.empty());

        String body = """
                {"cantidad": 1}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/reservar", "NO-EXISTE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservar_devuelve400SiCantidadNoEsPositiva() throws Exception {
        String body = """
                {"cantidad": 0}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/reservar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", is("cantidad: cantidad debe ser mayor que 0")));
    }

    @Test
    void liberar_devuelve200() throws Exception {
        Stock liberado = stock("SKU-1", 10, 1);
        when(stockService.liberar(eq("SKU-1"), any()))
                .thenReturn(Optional.of(liberado));

        String body = """
                {"cantidad": 4}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/liberar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku", is("SKU-1")))
                .andExpect(jsonPath("$.cantidadReservada", is(1)));
    }

    @Test
    void liberar_devuelve409SiNoHayReservadoSuficiente() throws Exception {
        when(stockService.liberar(eq("SKU-1"), any()))
                .thenThrow(new StockConflictException("No hay suficiente stock reservado"));

        String body = """
                {"cantidad": 50}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/liberar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", is("No hay suficiente stock reservado")));
    }

    @Test
    void confirmar_devuelve200() throws Exception {
        Stock confirmado = stock("SKU-1", 7, 2);
        when(stockService.confirmar(eq("SKU-1"), any()))
                .thenReturn(Optional.of(confirmado));

        String body = """
                {"cantidad": 3, "refOrden": "ord-2"}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/confirmar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible", is(7)))
                .andExpect(jsonPath("$.cantidadReservada", is(2)));
    }

    @Test
    void confirmar_devuelve409SiNoHayStockSuficiente() throws Exception {
        when(stockService.confirmar(eq("SKU-1"), any()))
                .thenThrow(new StockConflictException("No hay suficiente stock reservado"));

        String body = """
                {"cantidad": 50}
                """;

        mockMvc.perform(post("/api/v1/stock/{sku}/confirmar", "SKU-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", is("No hay suficiente stock reservado")));
    }

    private Stock stock(String sku, int disponible, int reservada) {
        Stock stock = new Stock(sku, disponible, reservada);
        stock.setId(1L);
        stock.setFechaActualizacion(LocalDateTime.of(2026, 9, 4, 10, 30));
        return stock;
    }
}
