package com.buildzone.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un plan de suscripcion - solo ADMIN. */
public record PlanRequest(
        @NotBlank(message = "El nombre del plan es obligatorio.")
        @Size(max = 100, message = "El nombre del plan no puede superar 100 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres.")
        String descripcion,

        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(value = "0", message = "El precio no puede ser negativo.")
        BigDecimal precio,

        @NotNull(message = "La duracion en dias es obligatoria.")
        @Min(value = 1, message = "La duracion debe ser de al menos 1 dia.")
        @Max(value = 3650, message = "La duracion no puede superar 3650 dias.")
        Integer duracionDias) {
}
