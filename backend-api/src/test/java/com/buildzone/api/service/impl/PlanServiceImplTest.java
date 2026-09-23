package com.buildzone.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buildzone.api.Fixtures;
import com.buildzone.api.dto.PlanRequest;
import com.buildzone.api.dto.PlanResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.repository.PlanSuscripcionRepository;

/** Pruebas unitarias del modulo de planes. */
@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanSuscripcionRepository planRepository;
    @InjectMocks
    private PlanServiceImpl planService;

    private final PlanRequest premium = new PlanRequest("Premium", "Sin limites", new BigDecimal("29900"), 30);

    @Test
    @DisplayName("Listar activos: solo devuelve lo que entrega el repositorio de activos")
    void listarActivos() {
        when(planRepository.findByActivoTrueOrderByPrecioAsc())
                .thenReturn(List.of(Fixtures.plan(1L, "Gratuito", 3650, true)));

        List<PlanResponse> planes = planService.listarActivos();

        assertThat(planes).extracting(PlanResponse::nombre).containsExactly("Gratuito");
    }

    @Test
    @DisplayName("Crear: guarda un plan nuevo activo")
    void crearPlan() {
        when(planRepository.existsByNombreIgnoreCase("Premium")).thenReturn(false);
        when(planRepository.save(any(PlanSuscripcion.class))).thenAnswer(inv -> {
            PlanSuscripcion p = inv.getArgument(0);
            p.setId(5L);
            return p;
        });

        PlanResponse creado = planService.crear(premium);

        assertThat(creado.id()).isEqualTo(5L);
        assertThat(creado.activo()).isTrue();
        assertThat(creado.duracionDias()).isEqualTo(30);
    }

    @Test
    @DisplayName("Crear: rechaza un nombre de plan repetido")
    void crearPlanDuplicado() {
        when(planRepository.existsByNombreIgnoreCase("Premium")).thenReturn(true);

        assertThatThrownBy(() -> planService.crear(premium)).isInstanceOf(RecursoDuplicadoException.class);
        verify(planRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desactivar: es un borrado logico (activo = false)")
    void desactivarPlan() {
        PlanSuscripcion plan = Fixtures.plan(2L, "Premium", 30, true);
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan));

        planService.desactivar(2L);

        assertThat(plan.isActivo()).isFalse();
        verify(planRepository).save(plan);
    }

    @Test
    @DisplayName("Actualizar: plan inexistente lanza 404")
    void actualizarInexistente() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.actualizar(99L, premium))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
