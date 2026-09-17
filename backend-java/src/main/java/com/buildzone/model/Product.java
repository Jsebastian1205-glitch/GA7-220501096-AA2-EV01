package com.buildzone.model;

/**
 * Representa un producto del catalogo de BuildZone.
 * Corresponde a la tabla "producto" de la base de datos.
 */
public class Product {

    private int idProduct;
    private int idBrand;
    private int idCategory;
    private String name;
    private String description;
    private String image;

    public Product() {
    }

    public Product(
            int idBrand,
            int idCategory,
            String name,
            String description,
            String image) {

        this.idBrand = idBrand;
        this.idCategory = idCategory;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public Product(
            int idProduct,
            int idBrand,
            int idCategory,
            String name,
            String description,
            String image) {

        this.idProduct = idProduct;
        this.idBrand = idBrand;
        this.idCategory = idCategory;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public int getIdBrand() {
        return idBrand;
    }

    public void setIdBrand(int idBrand) {
        this.idBrand = idBrand;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {

        return "Producto{" +
                "idProduct=" + idProduct +
                ", idBrand=" + idBrand +
                ", idCategory=" + idCategory +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
