package com.buildzone.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buildzone.api.dto.MarcaRequest;
import com.buildzone.api.dto.MarcaResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.Marca;
import com.buildzone.api.repository.MarcaRepository;

/** Pruebas unitarias del modulo de catalogo: Marca. */
@ExtendWith(MockitoExtension.class)
class MarcaServiceImplTest {

    @Mock
    private MarcaRepository marcaRepository;
    @InjectMocks
    private MarcaServiceImpl marcaService;

    private MarcaRequest request(String nombre) {
        MarcaRequest r = new MarcaRequest();
        r.setNombre(nombre);
        r.setDescripcion("Descripcion de " + nombre);
        return r;
    }

    @Test
    @DisplayName("Crear: guarda una marca con nombre nuevo")
    void crearMarca() {
        when(marcaRepository.existsByNombreIgnoreCase("Intel")).thenReturn(false);
        when(marcaRepository.save(any(Marca.class))).thenAnswer(inv -> {
            Marca m = inv.getArgument(0);
            m.setIdMarca(1);
            return m;
        });

        MarcaResponse r = marcaService.crear(request("Intel"));

        assertThat(r.getIdMarca()).isEqualTo(1);
        assertThat(r.getNombre()).isEqualTo("Intel");
    }

    @Test
    @DisplayName("Crear: rechaza un nombre repetido (409)")
    void crearMarcaDuplicada() {
        when(marcaRepository.existsByNombreIgnoreCase("Intel")).thenReturn(true);

        assertThatThrownBy(() -> marcaService.crear(request("Intel")))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(marcaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar: se puede conservar el mismo nombre sin error de duplicado")
    void actualizarMismoNombre() {
        Marca intel = new Marca("Intel", "CPU");
        intel.setIdMarca(1);
        when(marcaRepository.findById(1)).thenReturn(Optional.of(intel));
        when(marcaRepository.save(intel)).thenReturn(intel);

        MarcaResponse r = marcaService.actualizar(1, request("INTEL"));

        assertThat(r.getNombre()).isEqualTo("INTEL");
    }

    @Test
    @DisplayName("Eliminar: id inexistente lanza 404")
    void eliminarInexistente() {
        when(marcaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> marcaService.eliminar(99)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
