package com.califorge.msinventario.dto;

import com.califorge.msinventario.model.Stock;

import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        String sku,
        Integer cantidadDisponible,
        Integer cantidadReservada,
        LocalDateTime fechaActualizacion) {

    public static StockResponse desde(Stock s) {
        return new StockResponse(
                s.getId(),
                s.getSku(),
                s.getCantidadDisponible(),
                s.getCantidadReservada(),
                s.getFechaActualizacion());
    }
}