package com.buildzone.dao;

import com.buildzone.config.DatabaseConnection;
import com.buildzone.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (DAO) para la entidad Product.
 * Implementa las cuatro operaciones basicas sobre la tabla "producto":
 * insertar, consultar, actualizar y eliminar (CRUD), usando JDBC.
 */
public class ProductDAO {

    // INSERTAR
    public boolean insertProduct(Product product) {

        String sql = """
                INSERT INTO producto
                (id_marca, id_categoria, nombre, descripcion, imagen)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, product.getIdBrand());
            statement.setInt(2, product.getIdCategory());
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getImage());

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("Error al insertar el producto.");
            e.printStackTrace();

            return false;
        }
    }

    // CONSULTAR (todos, con nombre de marca y categoria)
    public List<Product> getAllProducts() {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT id_producto, id_marca, id_categoria,
                       nombre, descripcion, imagen
                FROM producto
                ORDER BY id_producto
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Product product = new Product(
                        resultSet.getInt("id_producto"),
                        resultSet.getInt("id_marca"),
                        resultSet.getInt("id_categoria"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        resultSet.getString("imagen")
                );

                products.add(product);
            }

        } catch (SQLException e) {

            System.out.println("Error al consultar productos.");
            e.printStackTrace();
        }

        return products;
    }

    // CONSULTAR (uno solo, por id, para precargar datos antes de actualizar)
    public Product getProductById(int idProduct) {

        String sql = """
                SELECT id_producto, id_marca, id_categoria,
                       nombre, descripcion, imagen
                FROM producto
                WHERE id_producto = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idProduct);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return new Product(
                            resultSet.getInt("id_producto"),
                            resultSet.getInt("id_marca"),
                            resultSet.getInt("id_categoria"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            resultSet.getString("imagen")
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al consultar el producto.");
            e.printStackTrace();
        }

        return null;
    }

    // CONSULTAR (listado detallado con nombre de marca y categoria, para reportes)
    public void printProductsWithDetails() {

        String sql = """
                SELECT
                    p.id_producto,
                    p.nombre,
                    m.nombre AS marca,
                    c.nombre AS categoria,
                    p.descripcion
                FROM producto p
                INNER JOIN marca m
                    ON p.id_marca = m.id_marca
                INNER JOIN categoria c
                    ON p.id_categoria = c.id_categoria
                ORDER BY p.id_producto
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                System.out.println("ID: " + resultSet.getInt("id_producto"));
                System.out.println("Producto: " + resultSet.getString("nombre"));
                System.out.println("Marca: " + resultSet.getString("marca"));
                System.out.println("Categoria: " + resultSet.getString("categoria"));
                System.out.println("Descripcion: " + resultSet.getString("descripcion"));
                System.out.println("----------------------");
            }

        } catch (SQLException e) {

            System.out.println("Error al consultar productos.");
            e.printStackTrace();
        }
    }

    // ACTUALIZAR
    public boolean updateProduct(Product product) {

        String sql = """
                UPDATE producto
                SET id_marca = ?,
                    id_categoria = ?,
                    nombre = ?,
                    descripcion = ?,
                    imagen = ?
                WHERE id_producto = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, product.getIdBrand());
            statement.setInt(2, product.getIdCategory());
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getImage());
            statement.setInt(6, product.getIdProduct());

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar el producto.");
            e.printStackTrace();

            return false;
        }
    }

    // ELIMINAR
    public boolean deleteProduct(int idProduct) {

        String sql = """
                DELETE FROM producto
                WHERE id_producto = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idProduct);

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al eliminar el producto.");
            e.printStackTrace();

            return false;
        }
    }
}
