package com.buildzone.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.dto.PlanRequest;
import com.buildzone.api.dto.PlanResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.repository.PlanSuscripcionRepository;
import com.buildzone.api.service.PlanService;

/**
 * Implementacion de {@link PlanService}. "Eliminar" un plan es en
 * realidad desactivarlo (borrado logico): deja de ofrecerse, pero el
 * historial de suscripciones que lo usan se conserva.
 */
@Service
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

    private final PlanSuscripcionRepository planRepository;

    public PlanServiceImpl(PlanSuscripcionRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public List<PlanResponse> listarActivos() {
        return planRepository.findByActivoTrueOrderByPrecioAsc().stream()
                .map(PlanResponse::desde)
                .toList();
    }

    @Override
    public List<PlanResponse> listarTodos() {
        return planRepository.findAllByOrderByIdAsc().stream()
                .map(PlanResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public PlanResponse crear(PlanRequest request) {
        String nombre = request.nombre().trim();
        if (planRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe un plan con el nombre '" + nombre + "'.");
        }
        PlanSuscripcion plan = new PlanSuscripcion(nombre, request.descripcion(), request.precio(),
                request.duracionDias());
        return PlanResponse.desde(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanResponse actualizar(Long id, PlanRequest request) {
        PlanSuscripcion plan = buscarOFallar(id);
        String nombre = request.nombre().trim();

        boolean cambiaNombre = !plan.getNombre().equalsIgnoreCase(nombre);
        if (cambiaNombre && planRepository.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe un plan con el nombre '" + nombre + "'.");
        }

        plan.setNombre(nombre);
        plan.setDescripcion(request.descripcion());
        plan.setPrecio(request.precio());
        plan.setDuracionDias(request.duracionDias());
        return PlanResponse.desde(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        PlanSuscripcion plan = buscarOFallar(id);
        plan.setActivo(false);
        planRepository.save(plan);
    }

    private PlanSuscripcion buscarOFallar(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un plan con id " + id));
    }
}
