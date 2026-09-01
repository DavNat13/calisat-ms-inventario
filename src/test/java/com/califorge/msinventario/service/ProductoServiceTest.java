package com.califorge.msinventario.service;

import com.califorge.msinventario.dto.ProductoRequest;
import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.model.Producto;
import com.califorge.msinventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crear_mapeaTodosLosCamposEditablesDelRequest() {
        when(productoRepository.existsBySku("SKU-1")).thenReturn(false);
        Producto guardado = new Producto();
        guardado.setId(UUID.randomUUID());
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        ProductoRequest request = new ProductoRequest(
                "SKU-1", "Barra", "Barras", "Barra de dominadas",
                new BigDecimal("10.00"), 5, "unidad");

        productoService.crear(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        Producto entidad = captor.getValue();
        assertEquals("SKU-1", entidad.getSku());
        assertEquals("Barra", entidad.getNombre());
        assertEquals("Barras", entidad.getCategoria());
        assertEquals("Barra de dominadas", entidad.getDescripcion());
        assertEquals(new BigDecimal("10.00"), entidad.getPrecio());
        assertEquals(5, entidad.getStock());
        assertEquals("unidad", entidad.getUnidadMedida());
    }

    @Test
    void crear_lanzaSkuDuplicado() {
        when(productoRepository.existsBySku("SKU-1")).thenReturn(true);
        ProductoRequest request = new ProductoRequest(
                "SKU-1", "Barra", null, null, null, null, null);

        assertThrows(SkuDuplicadoException.class, () -> productoService.crear(request));
        verify(productoRepository).existsBySku("SKU-1");
    }

    @Test
    void listar_devuelveSoloActivos() {
        Producto activo = producto("SKU-1", "Barra", true);
        Producto inactivo = producto("SKU-2", "Paralelas", false);
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(activo));

        List<Producto> resultado = productoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("SKU-1", resultado.get(0).getSku());
        verify(productoRepository).findByActivoTrue();
    }

    @Test
    void buscarPorId_devuelveProductoActivo() {
        UUID id = UUID.randomUUID();
        Producto activo = producto("SKU-1", "Barra", true);
        activo.setId(id);
        when(productoRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(activo));

        Optional<Producto> resultado = productoService.buscarPorId(id);

        assertTrue(resultado.isPresent());
        assertEquals("SKU-1", resultado.get().getSku());
    }

    @Test
    void buscarPorId_noDevuelveProductoInactivo() {
        UUID id = UUID.randomUUID();
        when(productoRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.empty());

        Optional<Producto> resultado = productoService.buscarPorId(id);

        assertFalse(resultado.isPresent());
    }

    private Producto producto(String sku, String nombre, boolean activo) {
        Producto p = new Producto(sku, nombre, new BigDecimal("10.00"), 5);
        p.setActivo(activo);
        return p;
    }
}
