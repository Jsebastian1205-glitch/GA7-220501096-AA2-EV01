package com.buildzone.api.dto;

import com.buildzone.api.model.Categoria;

/**
 * Datos de salida de una Categoria.
 */
public class CategoriaResponse {

    private Integer idCategoria;
    private String nombre;
    private String descripcion;

    public CategoriaResponse() {
    }

    public CategoriaResponse(Categoria categoria) {
        this.idCategoria = categoria.getIdCategoria();
        this.nombre = categoria.getNombre();
        this.descripcion = categoria.getDescripcion();
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
