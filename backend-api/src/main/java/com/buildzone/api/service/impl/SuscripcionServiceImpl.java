package com.buildzone.api.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.dto.SuscripcionResponse;
import com.buildzone.api.exception.AccesoDenegadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.exception.ReglaNegocioException;
import com.buildzone.api.model.PlanSuscripcion;
import com.buildzone.api.model.Suscripcion;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.EstadoSuscripcion;
import com.buildzone.api.repository.PlanSuscripcionRepository;
import com.buildzone.api.repository.SuscripcionRepository;
import com.buildzone.api.repository.UsuarioRepository;
import com.buildzone.api.security.UsuarioAutenticado;
import com.buildzone.api.service.SuscripcionService;

/**
 * Implementacion de {@link SuscripcionService}.
 * <p>
 * Reglas de negocio:
 * <ol>
 *   <li>Solo se puede suscribir a planes activos.</li>
 *   <li>Un usuario tiene como maximo una suscripcion ACTIVA: al cambiar
 *       de plan, la anterior pasa a CANCELADA.</li>
 *   <li>La fecha de fin es la fecha de inicio + la duracion del plan.</li>
 *   <li>Una suscripcion ACTIVA cuya fecha de fin ya paso se marca como
 *       VENCIDA la proxima vez que se consulta.</li>
 *   <li>Solo el dueno (o un ADMIN) puede cancelar una suscripcion, y solo
 *       si esta ACTIVA.</li>
 * </ol>
 * Se inyecta un {@link Clock} en lugar de llamar a {@code LocalDate.now()}
 * directamente, para poder fijar la fecha en las pruebas unitarias.
 */
@Service
@Transactional(readOnly = true)
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final PlanSuscripcionRepository planRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public SuscripcionServiceImpl(SuscripcionRepository suscripcionRepository,
                                  PlanSuscripcionRepository planRepository,
                                  UsuarioRepository usuarioRepository,
                                  Clock clock) {
        this.suscripcionRepository = suscripcionRepository;
        this.planRepository = planRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SuscripcionResponse suscribir(Long usuarioId, Long planId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + usuarioId));
        PlanSuscripcion plan = planRepository.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un plan con id " + planId));

        if (!plan.isActivo()) {
            throw new ReglaNegocioException("El plan '" + plan.getNombre() + "' ya no esta disponible.");
        }

        List<Suscripcion> activas = suscripcionRepository.findByUsuarioIdAndEstado(usuarioId,
                EstadoSuscripcion.ACTIVA);
        boolean yaTieneEstePlan = activas.stream()
                .anyMatch(s -> s.getPlan().getId().equals(planId) && !s.debeVencer(hoy()));
        if (yaTieneEstePlan) {
            throw new ReglaNegocioException("Ya tienes una suscripcion activa a este plan.");
        }
        activas.forEach(s -> s.setEstado(s.debeVencer(hoy())
                ? EstadoSuscripcion.VENCIDA
                : EstadoSuscripcion.CANCELADA));
        suscripcionRepository.saveAll(activas);

        LocalDate inicio = hoy();
        Suscripcion nueva = new Suscripcion(usuario, plan, inicio, inicio.plusDays(plan.getDuracionDias()));
        return SuscripcionResponse.desde(suscripcionRepository.save(nueva));
    }

    @Override
    @Transactional
    public Optional<SuscripcionResponse> obtenerActiva(Long usuarioId) {
        List<Suscripcion> activas = suscripcionRepository.findByUsuarioIdAndEstado(usuarioId,
                EstadoSuscripcion.ACTIVA);
        marcarVencidas(activas);
        return activas.stream()
                .filter(s -> s.getEstado() == EstadoSuscripcion.ACTIVA)
                .findFirst()
                .map(SuscripcionResponse::desde);
    }

    @Override
    @Transactional
    public List<SuscripcionResponse> historial(Long usuarioId) {
        List<Suscripcion> historial = suscripcionRepository.findByUsuarioIdOrderByFechaInicioDescIdDesc(usuarioId);
        marcarVencidas(historial);
        return historial.stream().map(SuscripcionResponse::desde).toList();
    }

    @Override
    @Transactional
    public void cancelar(UsuarioAutenticado actor, Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una suscripcion con id " + suscripcionId));

        boolean esDuenio = suscripcion.getUsuario().getId().equals(actor.id());
        if (!esDuenio && !actor.esAdmin()) {
            throw new AccesoDenegadoException("No puedes cancelar la suscripcion de otro usuario.");
        }
        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden cancelar suscripciones activas.");
        }

        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcionRepository.save(suscripcion);
    }

    @Override
    @Transactional
    public List<SuscripcionResponse> listarTodas() {
        List<Suscripcion> todas = suscripcionRepository.findAllByOrderByIdDesc();
        marcarVencidas(todas);
        return todas.stream().map(SuscripcionResponse::desde).toList();
    }

    /** Pasa a VENCIDA las suscripciones ACTIVA cuya fecha de fin ya paso. */
    private void marcarVencidas(List<Suscripcion> suscripciones) {
        LocalDate hoy = hoy();
        List<Suscripcion> vencidas = suscripciones.stream().filter(s -> s.debeVencer(hoy)).toList();
        vencidas.forEach(s -> s.setEstado(EstadoSuscripcion.VENCIDA));
        if (!vencidas.isEmpty()) {
            suscripcionRepository.saveAll(vencidas);
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(clock);
    }
}
