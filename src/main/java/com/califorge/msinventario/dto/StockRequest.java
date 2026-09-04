package com.califorge.msinventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear/actualizar un registro de stock.
 * Solo expone los campos editables del dominio (punto 3 de INSTRUCCIONES.md),
 * excluyendo {@code id} y {@code fechaActualizacion} que no se inyectan desde el request.
 */
public record StockRequest(
        @NotBlank(message = "sku es obligatorio")
        @Size(max = 64, message = "sku no puede superar 64 caracteres")
        String sku,

        @Min(value = 0, message = "cantidadDisponible no puede ser negativa")
        Integer cantidadDisponible,

        @Min(value = 0, message = "cantidadReservada no puede ser negativa")
        Integer cantidadReservada) {
}