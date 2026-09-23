package com.buildzone.api.dto;

import com.buildzone.api.model.enums.Rol;

import jakarta.validation.constraints.NotNull;

/** Cambio de rol de un usuario (PATCH /api/usuarios/{id}/rol) - solo ADMIN. */
public record CambioRolRequest(@NotNull(message = "El rol es obligatorio.") Rol rol) {
}
