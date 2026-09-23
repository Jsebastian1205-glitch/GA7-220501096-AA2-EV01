package com.buildzone.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos editables del propio perfil (PUT /api/usuarios/me). */
public record PerfilRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 60, message = "El apellido no puede superar 60 caracteres.")
        String apellido,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato valido.")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres.")
        String email) {
}
