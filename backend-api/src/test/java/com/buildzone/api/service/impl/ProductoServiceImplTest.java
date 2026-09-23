package com.buildzone.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buildzone.api.dto.ProductoRequest;
import com.buildzone.api.dto.ProductoResponse;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.Categoria;
import com.buildzone.api.model.Marca;
import com.buildzone.api.model.Producto;
import com.buildzone.api.repository.CategoriaRepository;
import com.buildzone.api.repository.MarcaRepository;
import com.buildzone.api.repository.ProductoRepository;

/** Pruebas unitarias del modulo de catalogo: Producto. */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MarcaRepository marcaRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @InjectMocks
    private ProductoServiceImpl productoService;

    private Marca amd;
    private Categoria procesador;

    @BeforeEach
    void preparar() {
        amd = new Marca("AMD", null);
        amd.setIdMarca(2);
        procesador = new Categoria("Procesador", null);
        procesador.setIdCategoria(1);
    }

    private ProductoRequest request(Integer idMarca, Integer idCategoria) {
        ProductoRequest r = new ProductoRequest();
        r.setNombre("Ryzen 7 7700X");
        r.setDescripcion("8 nucleos");
        r.setIdMarca(idMarca);
        r.setIdCategoria(idCategoria);
        return r;
    }

    @Test
    @DisplayName("Crear: responde con el nombre de marca y categoria resueltos")
    void crearProducto() {
        when(marcaRepository.findById(2)).thenReturn(Optional.of(amd));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(procesador));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setIdProducto(15);
            return p;
        });

        ProductoResponse r = productoService.crear(request(2, 1));

        assertThat(r.getIdProducto()).isEqualTo(15);
        assertThat(r.getNombreMarca()).isEqualTo("AMD");
        assertThat(r.getNombreCategoria()).isEqualTo("Procesador");
    }

    @Test
    @DisplayName("Crear: una marca inexistente lanza 404 y no guarda nada")
    void crearConMarcaInexistente() {
        when(marcaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(request(99, 1)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("marca");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener: id inexistente lanza 404")
    void obtenerInexistente() {
        when(productoRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(404))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
