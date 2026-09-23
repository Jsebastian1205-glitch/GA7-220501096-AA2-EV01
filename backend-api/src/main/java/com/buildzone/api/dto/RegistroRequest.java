package com.buildzone.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear una cuenta nueva (POST /api/auth/registro). */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 60, message = "El apellido no puede superar 60 caracteres.")
        String apellido,

        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(min = 3, max = 40, message = "El nombre de usuario debe tener entre 3 y 40 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo admite letras, numeros, punto, guion y guion bajo.")
        String username,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato valido.")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres.")
        String email,

        @NotBlank(message = "La contrasena es obligatoria.")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres.")
        String password) {
}
