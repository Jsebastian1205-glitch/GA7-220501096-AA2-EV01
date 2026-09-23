package com.buildzone.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildzone.api.model.Suscripcion;
import com.buildzone.api.model.enums.EstadoSuscripcion;

/** Acceso a datos de {@link Suscripcion}. */
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    /** Historial de un usuario, de la mas reciente a la mas antigua. */
    List<Suscripcion> findByUsuarioIdOrderByFechaInicioDescIdDesc(Long usuarioId);

    List<Suscripcion> findByUsuarioIdAndEstado(Long usuarioId, EstadoSuscripcion estado);

    List<Suscripcion> findAllByOrderByIdDesc();
}
