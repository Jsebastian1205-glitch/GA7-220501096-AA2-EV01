package com.buildzone.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesion (POST /api/auth/login).
 *
 * @param identificador correo o nombre de usuario
 * @param password      contrasena en texto plano (viaja solo por HTTPS en produccion)
 */
public record LoginRequest(
        @NotBlank(message = "El correo o usuario es obligatorio.") String identificador,
        @NotBlank(message = "La contrasena es obligatoria.") String password) {
}
