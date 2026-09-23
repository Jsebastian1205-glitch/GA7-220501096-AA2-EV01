package com.buildzone.api.dto;

import jakarta.validation.constraints.NotNull;

/** Suscribirse a un plan (POST /api/suscripciones). */
public record SuscripcionRequest(@NotNull(message = "El plan es obligatorio.") Long planId) {
}
