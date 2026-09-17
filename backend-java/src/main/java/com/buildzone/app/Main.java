package com.buildzone.app;

import com.buildzone.dao.BrandDAO;
import com.buildzone.dao.CategoryDAO;
import com.buildzone.dao.ProductDAO;
import com.buildzone.model.Brand;
import com.buildzone.model.Category;
import com.buildzone.model.Product;

import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada del modulo BuildZone.
 * Presenta un menu de consola que permite ejecutar las operaciones
 * de insercion, consulta, actualizacion y eliminacion (CRUD) sobre
 * productos, marcas y categorias, usando los DAO conectados por JDBC.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final ProductDAO productDAO = new ProductDAO();
    private static final BrandDAO brandDAO = new BrandDAO();
    private static final CategoryDAO categoryDAO = new CategoryDAO();

    public static void main(String[] args) {

        int option;

        do {
            System.out.println("\n===== BuildZone - Modulo de Gestion =====");
            System.out.println("1. Gestionar productos");
            System.out.println("2. Gestionar marcas");
            System.out.println("3. Gestionar categorias");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opcion: ");

            option = readInt();

            switch (option) {
                case 1 -> productMenu();
                case 2 -> brandMenu();
                case 3 -> categoryMenu();
                case 0 -> System.out.println("Cerrando BuildZone...");
                default -> System.out.println("Opcion invalida.");
            }

        } while (option != 0);

        scanner.close();
    }

    // ================= PRODUCTOS =================

    private static void productMenu() {

        int option;

        do {
            System.out.println("\n--- Gestion de productos ---");
            System.out.println("1. Insertar producto");
            System.out.println("2. Consultar productos");
            System.out.println("3. Actualizar producto");
            System.out.println("4. Eliminar producto");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            option = readInt();

            switch (option) {
                case 1 -> insertProduct();
                case 2 -> productDAO.printProductsWithDetails();
                case 3 -> updateProduct();
                case 4 -> deleteProduct();
                case 0 -> { /* volver */ }
                default -> System.out.println("Opcion invalida.");
            }

        } while (option != 0);
    }

    private static void insertProduct() {

        System.out.print("ID de la marca: ");
        int idBrand = readInt();

        System.out.print("ID de la categoria: ");
        int idCategory = readInt();

        System.out.print("Nombre del producto: ");
        String name = scanner.nextLine();

        System.out.print("Descripcion: ");
        String description = scanner.nextLine();

        System.out.print("Ruta o nombre de la imagen: ");
        String image = scanner.nextLine();

        Product product = new Product(idBrand, idCategory, name, description, image);

        boolean success = productDAO.insertProduct(product);

        System.out.println(success
                ? "Producto insertado correctamente."
                : "No se pudo insertar el producto.");
    }

    private static void updateProduct() {

        System.out.print("ID del producto a actualizar: ");
        int idProduct = readInt();

        Product current = productDAO.getProductById(idProduct);

        if (current == null) {
            System.out.println("No existe un producto con ese ID.");
            return;
        }

        System.out.println("Datos actuales: " + current);

        System.out.print("Nueva marca (ID) [" + current.getIdBrand() + "]: ");
        int idBrand = readInt();

        System.out.print("Nueva categoria (ID) [" + current.getIdCategory() + "]: ");
        int idCategory = readInt();

        System.out.print("Nuevo nombre [" + current.getName() + "]: ");
        String name = scanner.nextLine();

        System.out.print("Nueva descripcion [" + current.getDescription() + "]: ");
        String description = scanner.nextLine();

        System.out.print("Nueva imagen [" + current.getImage() + "]: ");
        String image = scanner.nextLine();

        Product updated = new Product(idProduct, idBrand, idCategory, name, description, image);

        boolean success = productDAO.updateProduct(updated);

        System.out.println(success
                ? "Producto actualizado correctamente."
                : "No se pudo actualizar el producto.");
    }

    private static void deleteProduct() {

        System.out.print("ID del producto a eliminar: ");
        int idProduct = readInt();

        boolean success = productDAO.deleteProduct(idProduct);

        System.out.println(success
                ? "Producto eliminado correctamente."
                : "No se pudo eliminar el producto (verifique el ID).");
    }

    // ================= MARCAS =================

    private static void brandMenu() {

        int option;

        do {
            System.out.println("\n--- Gestion de marcas ---");
            System.out.println("1. Insertar marca");
            System.out.println("2. Consultar marcas");
            System.out.println("3. Actualizar marca");
            System.out.println("4. Eliminar marca");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            option = readInt();

            switch (option) {
                case 1 -> insertBrand();
                case 2 -> listBrands();
                case 3 -> updateBrand();
                case 4 -> deleteBrand();
                case 0 -> { /* volver */ }
                default -> System.out.println("Opcion invalida.");
            }

        } while (option != 0);
    }

    private static void insertBrand() {

        System.out.print("Nombre de la marca: ");
        String name = scanner.nextLine();

        System.out.print("Descripcion de la marca: ");
        String description = scanner.nextLine();

        boolean success = brandDAO.insertBrand(new Brand(name, description));

        System.out.println(success
                ? "Marca insertada correctamente."
                : "No se pudo insertar la marca.");
    }

    private static void listBrands() {

        List<Brand> brands = brandDAO.getAllBrands();

        if (brands.isEmpty()) {
            System.out.println("No hay marcas registradas.");
            return;
        }

        for (Brand brand : brands) {
            System.out.println(brand);
        }
    }

    private static void updateBrand() {

        System.out.print("ID de la marca a actualizar: ");
        int idBrand = readInt();

        System.out.print("Nuevo nombre: ");
        String name = scanner.nextLine();

        System.out.print("Nueva descripcion: ");
        String description = scanner.nextLine();

        boolean success = brandDAO.updateBrand(new Brand(idBrand, name, description));

        System.out.println(success
                ? "Marca actualizada correctamente."
                : "No se pudo actualizar la marca (verifique el ID).");
    }

    private static void deleteBrand() {

        System.out.print("ID de la marca a eliminar: ");
        int idBrand = readInt();

        boolean success = brandDAO.deleteBrand(idBrand);

        System.out.println(success
                ? "Marca eliminada correctamente."
                : "No se pudo eliminar la marca (verifique el ID).");
    }

    // ================= CATEGORIAS =================

    private static void categoryMenu() {

        int option;

        do {
            System.out.println("\n--- Gestion de categorias ---");
            System.out.println("1. Insertar categoria");
            System.out.println("2. Consultar categorias");
            System.out.println("3. Actualizar categoria");
            System.out.println("4. Eliminar categoria");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            option = readInt();

            switch (option) {
                case 1 -> insertCategory();
                case 2 -> listCategories();
                case 3 -> updateCategory();
                case 4 -> deleteCategory();
                case 0 -> { /* volver */ }
                default -> System.out.println("Opcion invalida.");
            }

        } while (option != 0);
    }

    private static void insertCategory() {

        System.out.print("Nombre de la categoria: ");
        String name = scanner.nextLine();

        System.out.print("Descripcion de la categoria: ");
        String description = scanner.nextLine();

        boolean success = categoryDAO.insertCategory(new Category(name, description));

        System.out.println(success
                ? "Categoria insertada correctamente."
                : "No se pudo insertar la categoria.");
    }

    private static void listCategories() {

        List<Category> categories = categoryDAO.getAllCategories();

        if (categories.isEmpty()) {
            System.out.println("No hay categorias registradas.");
            return;
        }

        for (Category category : categories) {
            System.out.println(category);
        }
    }

    private static void updateCategory() {

        System.out.print("ID de la categoria a actualizar: ");
        int idCategory = readInt();

        System.out.print("Nuevo nombre: ");
        String name = scanner.nextLine();

        System.out.print("Nueva descripcion: ");
        String description = scanner.nextLine();

        boolean success = categoryDAO.updateCategory(new Category(idCategory, name, description));

        System.out.println(success
                ? "Categoria actualizada correctamente."
                : "No se pudo actualizar la categoria (verifique el ID).");
    }

    private static void deleteCategory() {

        System.out.print("ID de la categoria a eliminar: ");
        int idCategory = readInt();

        boolean success = categoryDAO.deleteCategory(idCategory);

        System.out.println(success
                ? "Categoria eliminada correctamente."
                : "No se pudo eliminar la categoria (verifique el ID).");
    }

    // ================= UTILIDADES =================

    /**
     * Lee un numero entero de forma segura y consume el salto de linea
     * pendiente, para que las siguientes lecturas con nextLine() no
     * capturen una cadena vacia.
     */
    private static int readInt() {

        while (!scanner.hasNextInt()) {
            System.out.print("Ingrese un numero valido: ");
            scanner.next();
        }

        int value = scanner.nextInt();
        scanner.nextLine();

        return value;
    }
}
