package com.buildzone.api.dto;

import com.buildzone.api.model.Marca;

/**
 * Datos de salida de una Marca. Se usa un DTO de respuesta (en vez de
 * devolver la entidad directamente) para tener control explicito de lo
 * que la API expone, aunque hoy coincide con los campos de la entidad.
 */
public class MarcaResponse {

    private Integer idMarca;
    private String nombre;
    private String descripcion;

    public MarcaResponse() {
    }

    /**
     * Construye la respuesta a partir de la entidad JPA.
     *
     * @param marca entidad ya guardada/leida de la base de datos
     */
    public MarcaResponse(Marca marca) {
        this.idMarca = marca.getIdMarca();
        this.nombre = marca.getNombre();
        this.descripcion = marca.getDescripcion();
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
