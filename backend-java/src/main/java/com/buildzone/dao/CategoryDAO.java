package com.buildzone.dao;

import com.buildzone.config.DatabaseConnection;
import com.buildzone.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (DAO) para la entidad Category.
 * Implementa las cuatro operaciones basicas sobre la tabla "categoria":
 * insertar, consultar, actualizar y eliminar (CRUD), usando JDBC.
 */
public class CategoryDAO {

    // INSERTAR
    public boolean insertCategory(Category category) {

        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("Error al insertar la categoria.");
            e.printStackTrace();

            return false;
        }
    }

    // CONSULTAR
    public List<Category> getAllCategories() {

        List<Category> categories = new ArrayList<>();

        String sql = "SELECT id_categoria, nombre, descripcion FROM categoria ORDER BY id_categoria";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("id_categoria"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion")
                ));
            }

        } catch (SQLException e) {

            System.out.println("Error al consultar las categorias.");
            e.printStackTrace();
        }

        return categories;
    }

    // ACTUALIZAR
    public boolean updateCategory(Category category) {

        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, category.getIdCategory());

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar la categoria.");
            e.printStackTrace();

            return false;
        }
    }

    // ELIMINAR
    public boolean deleteCategory(int idCategory) {

        String sql = "DELETE FROM categoria WHERE id_categoria = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idCategory);

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al eliminar la categoria.");
            e.printStackTrace();

            return false;
        }
    }
}
