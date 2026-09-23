package com.buildzone.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cambio de contrasena propia (PUT /api/usuarios/me/password). */
public record CambioPasswordRequest(
        @NotBlank(message = "La contrasena actual es obligatoria.")
        String passwordActual,

        @NotBlank(message = "La contrasena nueva es obligatoria.")
        @Size(min = 8, max = 72, message = "La contrasena nueva debe tener entre 8 y 72 caracteres.")
        String passwordNueva) {
}
