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
 * insertar, consultar, actualizar y eliminar (CRUD), usando JDBC puro
 * (sentencias {@link PreparedStatement} y try-with-resources).
 */
public class CategoryDAO {

    /**
     * Inserta una nueva categoria.
     *
     * @param category categoria a guardar (nombre y descripcion); el
     *                 nombre debe ser unico segun la restriccion de
     *                 la tabla
     * @return true si se inserto correctamente; false si ocurrio un
     *         error (por ejemplo, un nombre repetido)
     */
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

    /**
     * Consulta todas las categorias registradas.
     *
     * @return lista de categorias (vacia si no hay registros o si
     *         ocurrio un error de conexion/consulta)
     */
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

    /**
     * Actualiza el nombre y la descripcion de una categoria existente.
     *
     * @param category categoria con el id_categoria a modificar y sus
     *                 nuevos datos
     * @return true si se actualizo al menos una fila; false si no
     *         existia una categoria con ese id o si ocurrio un error
     */
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

    /**
     * Elimina una categoria por su identificador.
     * Nota: si la categoria tiene productos asociados, la base de
     * datos rechazara la eliminacion por la llave foranea
     * fk_producto_categoria, y este metodo devolvera false.
     *
     * @param idCategory identificador de la categoria a eliminar
     * @return true si se elimino al menos una fila; false si no
     *         existia, si tiene productos asociados, o si ocurrio
     *         otro error
     */
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
