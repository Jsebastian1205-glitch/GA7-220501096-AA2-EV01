package com.buildzone.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.buildzone.api.dto.CategoriaRequest;
import com.buildzone.api.dto.CategoriaResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.Categoria;
import com.buildzone.api.repository.CategoriaRepository;
import com.buildzone.api.service.CategoriaService;

/**
 * Implementacion de {@link CategoriaService}, con la misma logica de
 * validacion de nombre unico que {@link MarcaServiceImpl}.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponse::new)
                .toList();
    }

    @Override
    public CategoriaResponse obtenerPorId(Integer id) {
        return new CategoriaResponse(buscarOFallar(id));
    }

    @Override
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una categoria con el nombre '" + request.getNombre() + "'");
        }
        Categoria categoria = new Categoria(request.getNombre(), request.getDescripcion());
        return new CategoriaResponse(categoriaRepository.save(categoria));
    }

    @Override
    public CategoriaResponse actualizar(Integer id, CategoriaRequest request) {
        Categoria categoria = buscarOFallar(id);

        boolean cambioDeNombre = !categoria.getNombre().equalsIgnoreCase(request.getNombre());
        if (cambioDeNombre && categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una categoria con el nombre '" + request.getNombre() + "'");
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return new CategoriaResponse(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminar(Integer id) {
        Categoria categoria = buscarOFallar(id);
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarOFallar(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una categoria con id " + id));
    }
}
