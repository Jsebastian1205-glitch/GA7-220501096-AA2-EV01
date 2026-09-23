package com.buildzone.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.dto.ProductoRequest;
import com.buildzone.api.dto.ProductoResponse;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.Categoria;
import com.buildzone.api.model.Marca;
import com.buildzone.api.model.Producto;
import com.buildzone.api.repository.CategoriaRepository;
import com.buildzone.api.repository.MarcaRepository;
import com.buildzone.api.repository.ProductoRepository;
import com.buildzone.api.service.ProductoService;

/**
 * Implementacion de {@link ProductoService}. A diferencia de Marca y
 * Categoria, crear o actualizar un Producto requiere primero resolver
 * (buscar) la Marca y la Categoria referenciadas por id, y fallar con un
 * mensaje claro si alguna de las dos no existe.
 * <p>
 * La clase se marca {@code @Transactional(readOnly = true)} por defecto:
 * como {@link Producto} carga su {@code marca} y {@code categoria} de
 * forma perezosa (LAZY), esos datos solo se pueden leer mientras la
 * transaccion/sesion de Hibernate sigue abierta. Sin esta anotacion, el
 * repositorio abre y cierra su propia transaccion en cada llamada, y al
 * intentar leer {@code producto.getMarca()} ya fuera de ella se lanza
 * {@code LazyInitializationException} (se manifiesta como un error 500).
 * Los metodos que escriben (crear/actualizar/eliminar) sobrescriben esto
 * con {@code @Transactional} de lectura-escritura.
 */
@Service
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                                MarcaRepository marcaRepository,
                                CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.marcaRepository = marcaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoResponse::new)
                .toList();
    }

    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        return new ProductoResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Marca marca = buscarMarcaOFallar(request.getIdMarca());
        Categoria categoria = buscarCategoriaOFallar(request.getIdCategoria());

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setImagen(request.getImagen());
        producto.setMarca(marca);
        producto.setCategoria(categoria);

        return new ProductoResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = buscarOFallar(id);
        Marca marca = buscarMarcaOFallar(request.getIdMarca());
        Categoria categoria = buscarCategoriaOFallar(request.getIdCategoria());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setImagen(request.getImagen());
        producto.setMarca(marca);
        producto.setCategoria(categoria);

        return new ProductoResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Producto producto = buscarOFallar(id);
        productoRepository.delete(producto);
    }

    private Producto buscarOFallar(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un producto con id " + id));
    }

    private Marca buscarMarcaOFallar(Integer idMarca) {
        return marcaRepository.findById(idMarca)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una marca con id " + idMarca));
    }

    private Categoria buscarCategoriaOFallar(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una categoria con id " + idCategoria));
    }
}