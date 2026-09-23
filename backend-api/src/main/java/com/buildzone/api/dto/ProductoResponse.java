package com.buildzone.api.dto;

import com.buildzone.api.model.Producto;

/**
 * Datos de salida de un Producto. Incluye el nombre de la marca y de la
 * categoria (no solo sus ids), para que quien consuma la API no tenga
 * que hacer otra llamada para mostrar esa informacion.
 */
public class ProductoResponse {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private String imagen;
    private Integer idMarca;
    private String nombreMarca;
    private Integer idCategoria;
    private String nombreCategoria;

    public ProductoResponse() {
    }

    /**
     * Construye la respuesta a partir de la entidad JPA ya cargada
     * (incluyendo marca y categoria, para poder leer sus nombres).
     *
     * @param producto entidad con marca y categoria accesibles
     */
    public ProductoResponse(Producto producto) {
        this.idProducto = producto.getIdProducto();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.imagen = producto.getImagen();
        this.idMarca = producto.getMarca().getIdMarca();
        this.nombreMarca = producto.getMarca().getNombre();
        this.idCategoria = producto.getCategoria().getIdCategoria();
        this.nombreCategoria = producto.getCategoria().getNombre();
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
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

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
}
