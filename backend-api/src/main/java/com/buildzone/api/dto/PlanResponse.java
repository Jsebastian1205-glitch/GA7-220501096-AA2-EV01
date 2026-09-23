package com.buildzone.api.dto;

import java.math.BigDecimal;

import com.buildzone.api.model.PlanSuscripcion;

/** Datos de salida de un plan de suscripcion. */
public record PlanResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer duracionDias,
        boolean activo) {

    public static PlanResponse desde(PlanSuscripcion p) {
        return new PlanResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getDuracionDias(), p.isActivo());
    }
}
