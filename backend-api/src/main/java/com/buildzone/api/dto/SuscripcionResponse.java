package com.buildzone.api.dto;

import java.time.LocalDate;

import com.buildzone.api.model.Suscripcion;
import com.buildzone.api.model.enums.EstadoSuscripcion;

/** Datos de salida de una suscripcion, con el plan completo embebido. */
public record SuscripcionResponse(
        Long id,
        Long usuarioId,
        PlanResponse plan,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSuscripcion estado) {

    public static SuscripcionResponse desde(Suscripcion s) {
        return new SuscripcionResponse(s.getId(), s.getUsuario().getId(), PlanResponse.desde(s.getPlan()),
                s.getFechaInicio(), s.getFechaFin(), s.getEstado());
    }
}
