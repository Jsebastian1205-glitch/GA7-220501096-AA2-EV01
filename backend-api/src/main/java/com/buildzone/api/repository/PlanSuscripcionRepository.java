package com.buildzone.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildzone.api.model.PlanSuscripcion;

/** Acceso a datos de {@link PlanSuscripcion}. */
public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {

    /** @return planes visibles al publico, del mas barato al mas caro */
    List<PlanSuscripcion> findByActivoTrueOrderByPrecioAsc();

    List<PlanSuscripcion> findAllByOrderByIdAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
