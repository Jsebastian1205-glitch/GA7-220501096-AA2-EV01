package com.buildzone.model;

/**
 * Representa una marca de producto.
 * Corresponde a la tabla "marca" de la base de datos.
 */
public class Brand {

    private int idBrand;
    private String name;
    private String description;

    public Brand() {
    }

    public Brand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Brand(int idBrand, String name, String description) {
        this.idBrand = idBrand;
        this.name = name;
        this.description = description;
    }

    public int getIdBrand() {
        return idBrand;
    }

    public void setIdBrand(int idBrand) {
        this.idBrand = idBrand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "idBrand=" + idBrand +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
