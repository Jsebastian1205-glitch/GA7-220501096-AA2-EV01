package com.buildzone.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.buildzone.api.dto.MarcaRequest;
import com.buildzone.api.dto.MarcaResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.Marca;
import com.buildzone.api.repository.MarcaRepository;
import com.buildzone.api.service.MarcaService;

/**
 * Implementacion de {@link MarcaService}: logica de negocio real sobre
 * Marca (validaciones de nombre unico, existencia antes de actualizar o
 * eliminar).
 */
@Service
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaServiceImpl(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Override
    public List<MarcaResponse> listar() {
        return marcaRepository.findAll().stream()
                .map(MarcaResponse::new)
                .toList();
    }

    @Override
    public MarcaResponse obtenerPorId(Integer id) {
        return new MarcaResponse(buscarOFallar(id));
    }

    @Override
    public MarcaResponse crear(MarcaRequest request) {
        if (marcaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una marca con el nombre '" + request.getNombre() + "'");
        }
        Marca marca = new Marca(request.getNombre(), request.getDescripcion());
        return new MarcaResponse(marcaRepository.save(marca));
    }

    @Override
    public MarcaResponse actualizar(Integer id, MarcaRequest request) {
        Marca marca = buscarOFallar(id);

        boolean cambioDeNombre = !marca.getNombre().equalsIgnoreCase(request.getNombre());
        if (cambioDeNombre && marcaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una marca con el nombre '" + request.getNombre() + "'");
        }

        marca.setNombre(request.getNombre());
        marca.setDescripcion(request.getDescripcion());
        return new MarcaResponse(marcaRepository.save(marca));
    }

    @Override
    public void eliminar(Integer id) {
        Marca marca = buscarOFallar(id);
        marcaRepository.delete(marca);
    }

    /**
     * Busca una marca por id o lanza {@link RecursoNoEncontradoException}
     * si no existe. Metodo privado reutilizado por obtener/actualizar/
     * eliminar para no repetir la misma validacion tres veces.
     */
    private Marca buscarOFallar(Integer id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una marca con id " + id));
    }
}
