package com.buildzone.api.service;

import java.util.List;

import com.buildzone.api.dto.CategoriaRequest;
import com.buildzone.api.dto.CategoriaResponse;

/**
 * Contrato de los casos de uso sobre Categoria.
 */
public interface CategoriaService {

    List<CategoriaResponse> listar();

    CategoriaResponse obtenerPorId(Integer id);

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse actualizar(Integer id, CategoriaRequest request);

    void eliminar(Integer id);
}
