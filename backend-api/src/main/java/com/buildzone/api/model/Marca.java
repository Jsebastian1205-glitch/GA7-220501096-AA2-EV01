package com.buildzone.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa una marca de producto.
 * <p>
 * Mapea la tabla {@code marca}, que ya existe en la base de datos
 * {@code buildzone} (columnas: id_marca, nombre, descripcion; con
 * restriccion UNIQUE sobre nombre).
 */
@Entity
@Table(name = "marca")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /** Constructor vacio requerido por JPA. */
    public Marca() {
    }

    /**
     * Crea una marca nueva (sin id, lo asigna la base de datos).
     *
     * @param nombre      nombre de la marca (debe ser unico)
     * @param descripcion descripcion opcional de la marca
     */
    public Marca(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
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
}
