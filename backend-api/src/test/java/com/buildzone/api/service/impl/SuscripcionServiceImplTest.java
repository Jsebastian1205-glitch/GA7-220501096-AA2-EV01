package com.buildzone.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buildzone.api.Fixtures;
import com.buildzone.api.dto.SuscripcionResponse;
import com.buildzone.api.exception.AccesoDenegadoException;
import com.buildzone.api.exception.ReglaNegocioException;
import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.model.Suscripcion;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.EstadoSuscripcion;
import com.buildzone.api.model.enums.Rol;
import com.buildzone.api.repository.PlanSuscripcionRepository;
import com.buildzone.api.repository.SuscripcionRepository;
import com.buildzone.api.repository.UsuarioRepository;
import com.buildzone.api.security.UsuarioAutenticado;

/**
 * Pruebas unitarias del modulo de suscripciones. Se usa un reloj fijo
 * (23/09/2026 en Bogota) para que las fechas calculadas sean exactas.
 */
@ExtendWith(MockitoExtension.class)
class SuscripcionServiceImplTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 23);

    @Mock
    private SuscripcionRepository suscripcionRepository;
    @Mock
    private PlanSuscripcionRepository planRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private SuscripcionServiceImpl servicio;

    private Usuario ana;
    private PlanSuscripcion gratuito;
    private PlanSuscripcion premium;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-09-23T15:00:00Z"), ZoneId.of("America/Bogota"));
        servicio = new SuscripcionServiceImpl(suscripcionRepository, planRepository, usuarioRepository, reloj);
        ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        gratuito = Fixtures.plan(1L, "Gratuito", 3650, true);
        premium = Fixtures.plan(2L, "Premium", 30, true);
    }

    private Suscripcion suscripcion(Long id, PlanSuscripcion plan, LocalDate inicio, LocalDate fin) {
        Suscripcion s = new Suscripcion(ana, plan, inicio, fin);
        s.setId(id);
        return s;
    }

    @Test
    @DisplayName("Suscribir: la fecha de fin es hoy + duracion del plan y queda ACTIVA")
    void suscribirCalculaFechas() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(planRepository.findById(2L)).thenReturn(Optional.of(premium));
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA)).thenReturn(List.of());
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        SuscripcionResponse r = servicio.suscribir(1L, 2L);

        assertThat(r.fechaInicio()).isEqualTo(HOY);
        assertThat(r.fechaFin()).isEqualTo(HOY.plusDays(30));
        assertThat(r.estado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        assertThat(r.plan().nombre()).isEqualTo("Premium");
        assertThat(r.usuarioId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Suscribir: al cambiar de plan, la suscripcion activa anterior pasa a CANCELADA")
    void suscribirCancelaLaAnterior() {
        Suscripcion anterior = suscripcion(7L, gratuito, HOY.minusDays(10), HOY.plusDays(100));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(planRepository.findById(2L)).thenReturn(Optional.of(premium));
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(List.of(anterior));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.suscribir(1L, 2L);

        assertThat(anterior.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
        verify(suscripcionRepository).saveAll(List.of(anterior));
    }

    @Test
    @DisplayName("Suscribir: no se permite repetir el mismo plan si sigue activo")
    void suscribirMismoPlan() {
        Suscripcion actual = suscripcion(7L, premium, HOY.minusDays(1), HOY.plusDays(29));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(planRepository.findById(2L)).thenReturn(Optional.of(premium));
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(List.of(actual));

        assertThatThrownBy(() -> servicio.suscribir(1L, 2L)).isInstanceOf(ReglaNegocioException.class);
        verify(suscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Suscribir: un plan desactivado no esta disponible")
    void suscribirPlanInactivo() {
        premium.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(planRepository.findById(2L)).thenReturn(Optional.of(premium));

        assertThatThrownBy(() -> servicio.suscribir(1L, 2L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no esta disponible");
    }

    @Test
    @DisplayName("Activa: una suscripcion cuya fecha de fin ya paso se marca VENCIDA y no se devuelve")
    void obtenerActivaMarcaVencida() {
        Suscripcion vieja = suscripcion(3L, premium, HOY.minusDays(40), HOY.minusDays(10));
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(List.of(vieja));

        Optional<SuscripcionResponse> activa = servicio.obtenerActiva(1L);

        assertThat(activa).isEmpty();
        assertThat(vieja.getEstado()).isEqualTo(EstadoSuscripcion.VENCIDA);
        verify(suscripcionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Activa: devuelve la suscripcion vigente")
    void obtenerActivaVigente() {
        Suscripcion vigente = suscripcion(4L, premium, HOY, HOY.plusDays(30));
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(List.of(vigente));

        assertThat(servicio.obtenerActiva(1L)).map(SuscripcionResponse::id).contains(4L);
    }

    @Test
    @DisplayName("Cancelar: el dueno puede cancelar su suscripcion activa")
    void cancelarPropia() {
        Suscripcion s = suscripcion(5L, premium, HOY, HOY.plusDays(30));
        when(suscripcionRepository.findById(5L)).thenReturn(Optional.of(s));

        servicio.cancelar(new UsuarioAutenticado(1L, "ana", Rol.USUARIO), 5L);

        assertThat(s.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
        verify(suscripcionRepository).save(s);
    }

    @Test
    @DisplayName("Cancelar: un USUARIO no puede cancelar la suscripcion de otro")
    void cancelarAjena() {
        Suscripcion s = suscripcion(5L, premium, HOY, HOY.plusDays(30));
        when(suscripcionRepository.findById(5L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> servicio.cancelar(new UsuarioAutenticado(2L, "luis", Rol.USUARIO), 5L))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThat(s.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
    }

    @Test
    @DisplayName("Cancelar: un ADMIN si puede cancelar la suscripcion de otro")
    void cancelarComoAdmin() {
        Suscripcion s = suscripcion(5L, premium, HOY, HOY.plusDays(30));
        when(suscripcionRepository.findById(5L)).thenReturn(Optional.of(s));

        servicio.cancelar(new UsuarioAutenticado(9L, "admin", Rol.ADMIN), 5L);

        assertThat(s.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
    }

    @Test
    @DisplayName("Cancelar: no se puede cancelar una suscripcion que ya no esta activa")
    void cancelarYaCancelada() {
        Suscripcion s = suscripcion(5L, premium, HOY, HOY.plusDays(30));
        s.setEstado(EstadoSuscripcion.CANCELADA);
        when(suscripcionRepository.findById(5L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> servicio.cancelar(new UsuarioAutenticado(1L, "ana", Rol.USUARIO), 5L))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
