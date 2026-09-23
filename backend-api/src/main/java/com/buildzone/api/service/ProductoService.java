package com.buildzone.api.service;

import java.util.List;

import com.buildzone.api.dto.ProductoRequest;
import com.buildzone.api.dto.ProductoResponse;

/**
 * Contrato de los casos de uso sobre Producto.
 */
public interface ProductoService {

    List<ProductoResponse> listar();

    ProductoResponse obtenerPorId(Integer id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Integer id, ProductoRequest request);

    void eliminar(Integer id);
}
