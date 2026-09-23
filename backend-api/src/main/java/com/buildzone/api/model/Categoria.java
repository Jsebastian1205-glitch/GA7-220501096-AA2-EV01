package com.buildzone.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa una categoria de producto.
 * <p>
 * Mapea la tabla {@code categoria}, que ya existe en la base de datos
 * {@code buildzone} (columnas: id_categoria, nombre, descripcion; con
 * restriccion UNIQUE sobre nombre).
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /** Constructor vacio requerido por JPA. */
    public Categoria() {
    }

    /**
     * Crea una categoria nueva (sin id, lo asigna la base de datos).
     *
     * @param nombre      nombre de la categoria (debe ser unico)
     * @param descripcion descripcion opcional de la categoria
     */
    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
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
