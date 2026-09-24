package com.califorge.msinventario.dto;

import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para movimientos de stock (reservar, liberar, confirmar).
 */
public record StockMovimientoRequest(
        @Positive(message = "cantidad debe ser mayor que 0")
        int cantidad,

        String refOrden) {
}
