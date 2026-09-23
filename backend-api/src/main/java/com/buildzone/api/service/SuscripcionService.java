package com.buildzone.api.service;

import java.util.List;
import java.util.Optional;

import com.buildzone.api.dto.SuscripcionResponse;
import com.buildzone.api.security.UsuarioAutenticado;

/** Casos de uso sobre suscripciones. */
public interface SuscripcionService {

    SuscripcionResponse suscribir(Long usuarioId, Long planId);

    Optional<SuscripcionResponse> obtenerActiva(Long usuarioId);

    List<SuscripcionResponse> historial(Long usuarioId);

    void cancelar(UsuarioAutenticado actor, Long suscripcionId);

    List<SuscripcionResponse> listarTodas();
}
