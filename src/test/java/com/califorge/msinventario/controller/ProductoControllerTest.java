package com.califorge.msinventario.controller;

import com.califorge.msinventario.dto.ProductoRequest;
import com.califorge.msinventario.dto.ProductoResponse;
import com.califorge.msinventario.model.Producto;
import com.califorge.msinventario.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba unitaria del ProductoController (Fase 9) usando Mockito para el
 * servicio y MockMvc standalone. No requiere base de datos ni contexto web
 * completo (en Spring Boot 4.x no existe @WebMvcTest; se usa standaloneSetup).
 *
 * La validacion {@code @Valid} se ejerce automaticamente al disparar requests
 * HTTP con body (Bean Validation + @RestControllerAdvice si se registrara).
 */
@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
    }

    @Test
    void listar_devuelveListaDeProductos() throws Exception {
        Producto p = new Producto("SKU-1", "Barra", new BigDecimal("10.00"), 5);
        p.setId(UUID.randomUUID());
        when(productoService.listar()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/inventario/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("SKU-1")));
    }

    @Test
    void buscarPorId_devuelveProducto() throws Exception {
        UUID id = UUID.randomUUID();
        Producto p = new Producto("SKU-1", "Barra", new BigDecimal("10.00"), 5);
        p.setId(id);
        when(productoService.buscarPorId(id)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/v1/inventario/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())));
    }

    @Test
    void buscarPorId_devuelve404SiNoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(productoService.buscarPorId(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/inventario/productos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_devuelve201ConLocation() throws Exception {
        UUID id = UUID.randomUUID();
        Producto guardado = new Producto("SKU-1", "Barra", new BigDecimal("10.00"), 5);
        guardado.setId(id);
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(guardado);

        String body = """
                {"sku":"SKU-1","nombre":"Barra","precio":10.00,"stock":5}
                """;

        mockMvc.perform(post("/api/v1/inventario/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/v1/inventario/productos/" + id)))
                .andExpect(jsonPath("$.sku", is("SKU-1")));
    }
}
