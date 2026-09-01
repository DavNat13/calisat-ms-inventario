package com.califorge.msinventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO de entrada (request) para crear/actualizar un producto.
 *
 * Solo expone los campos editables por el cliente. Bloquea campos de control
 * como {@code id}, {@code activo} y {@code fechaRegistro} que no deben poder
 * inyectarse desde el request (vs. usar la entidad JPA directamente).
 */
public record ProductoRequest(
        @NotBlank(message = "sku es obligatorio")
        @Size(max = 64, message = "sku no puede superar 64 caracteres")
        String sku,

        @NotBlank(message = "nombre es obligatorio")
        @Size(max = 255, message = "nombre no puede superar 255 caracteres")
        String nombre,

        @Size(max = 100, message = "categoria no puede superar 100 caracteres")
        String categoria,

        @Size(max = 1000, message = "descripcion no puede superar 1000 caracteres")
        String descripcion,

        @DecimalMin(value = "0", message = "precio no puede ser negativo")
        BigDecimal precio,

        @Min(value = 0, message = "stock no puede ser negativo")
        Integer stock,

        @Size(max = 50, message = "unidadMedida no puede superar 50 caracteres")
        String unidadMedida) {
}
