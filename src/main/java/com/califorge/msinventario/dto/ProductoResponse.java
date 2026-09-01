package com.califorge.msinventario.dto;

import com.califorge.msinventario.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductoResponse(
        UUID id,
        String sku,
        String nombre,
        String categoria,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String unidadMedida,
        Boolean activo,
        LocalDateTime fechaRegistro) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getSku(),
                p.getNombre(),
                p.getCategoria(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getStock(),
                p.getUnidadMedida(),
                p.getActivo(),
                p.getFechaRegistro());
    }
}
