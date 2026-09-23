package com.buildzone.api.model;

import java.time.LocalDate;

import com.buildzone.api.model.enums.EstadoSuscripcion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Suscripcion de un {@link Usuario} a un {@link PlanSuscripcion} durante
 * un rango de fechas. Un usuario tiene como maximo una suscripcion
 * ACTIVA a la vez (regla aplicada en la capa de servicio).
 */
@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanSuscripcion plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSuscripcion estado = EstadoSuscripcion.ACTIVA;

    /** Constructor vacio requerido por JPA. */
    public Suscripcion() {
    }

    public Suscripcion(Usuario usuario, PlanSuscripcion plan, LocalDate fechaInicio, LocalDate fechaFin) {
        this.usuario = usuario;
        this.plan = plan;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    /**
     * @param hoy fecha actual
     * @return true si esta ACTIVA pero su fecha de fin ya paso
     */
    public boolean debeVencer(LocalDate hoy) {
        return estado == EstadoSuscripcion.ACTIVA && fechaFin.isBefore(hoy);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PlanSuscripcion getPlan() {
        return plan;
    }

    public void setPlan(PlanSuscripcion plan) {
        this.plan = plan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoSuscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSuscripcion estado) {
        this.estado = estado;
    }
}
