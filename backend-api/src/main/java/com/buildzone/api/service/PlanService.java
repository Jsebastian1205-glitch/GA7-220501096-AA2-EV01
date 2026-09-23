package com.buildzone.api.service;

import java.util.List;

import com.buildzone.api.dto.PlanRequest;
import com.buildzone.api.dto.PlanResponse;

/** Casos de uso sobre planes de suscripcion. */
public interface PlanService {

    List<PlanResponse> listarActivos();

    List<PlanResponse> listarTodos();

    PlanResponse crear(PlanRequest request);

    PlanResponse actualizar(Long id, PlanRequest request);

    void desactivar(Long id);
}
