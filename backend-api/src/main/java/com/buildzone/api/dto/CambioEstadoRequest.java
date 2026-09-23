package com.buildzone.api.dto;

import com.buildzone.api.model.enums.EstadoUsuario;

import jakarta.validation.constraints.NotNull;

/** Activar/desactivar una cuenta (PATCH /api/usuarios/{id}/estado) - solo ADMIN. */
public record CambioEstadoRequest(@NotNull(message = "El estado es obligatorio.") EstadoUsuario estado) {
}
