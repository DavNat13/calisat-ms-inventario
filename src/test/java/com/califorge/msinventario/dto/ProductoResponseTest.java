package com.califorge.msinventario.dto;

import com.califorge.msinventario.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductoResponseTest {

    @Test
    void desde_mapeaTodosLosCamposDeLaEntidad() {
        Producto producto = new Producto("SKU-1", "Barra", new BigDecimal("10.00"), 5);
        UUID id = UUID.randomUUID();
        producto.setId(id);
        producto.setCategoria("Barras");
        producto.setDescripcion("Barra de dominadas");
        producto.setUnidadMedida("unidad");
        producto.setActivo(true);
        producto.setFechaRegistro(LocalDateTime.of(2026, 8, 31, 10, 30));

        ProductoResponse response = ProductoResponse.desde(producto);

        assertEquals(id, response.id());
        assertEquals("SKU-1", response.sku());
        assertEquals("Barra", response.nombre());
        assertEquals("Barras", response.categoria());
        assertEquals("Barra de dominadas", response.descripcion());
        assertEquals(new BigDecimal("10.00"), response.precio());
        assertEquals(5, response.stock());
        assertEquals("unidad", response.unidadMedida());
        assertEquals(true, response.activo());
        assertNotNull(response.fechaRegistro());
    }
}
