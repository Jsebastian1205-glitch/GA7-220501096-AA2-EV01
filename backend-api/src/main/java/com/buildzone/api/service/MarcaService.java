package com.buildzone.api.service;

import java.util.List;

import com.buildzone.api.dto.MarcaRequest;
import com.buildzone.api.dto.MarcaResponse;

/**
 * Contrato de los casos de uso sobre Marca. El controlador depende de
 * esta interfaz, no de la implementacion.
 */
public interface MarcaService {

    List<MarcaResponse> listar();

    MarcaResponse obtenerPorId(Integer id);

    MarcaResponse crear(MarcaRequest request);

    MarcaResponse actualizar(Integer id, MarcaRequest request);

    void eliminar(Integer id);
}
