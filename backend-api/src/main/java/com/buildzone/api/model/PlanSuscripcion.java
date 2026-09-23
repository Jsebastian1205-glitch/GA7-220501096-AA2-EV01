package com.buildzone.api.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Plan de suscripcion ofrecido por BuildZone (por ejemplo Gratuito o
 * Premium). Los planes no se borran fisicamente: se desactivan, para no
 * romper el historial de suscripciones que los referencian.
 */
@Entity
@Table(name = "plan_suscripcion")
public class PlanSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /** Precio en pesos colombianos (COP). */
    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /** Constructor vacio requerido por JPA. */
    public PlanSuscripcion() {
    }

    public PlanSuscripcion(String nombre, String descripcion, BigDecimal precio, Integer duracionDias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracionDias = duracionDias;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
