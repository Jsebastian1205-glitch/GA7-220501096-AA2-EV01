package com.buildzone.dao;

import com.buildzone.config.DatabaseConnection;
import com.buildzone.model.Brand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (DAO) para la entidad Brand.
 * Implementa las cuatro operaciones basicas sobre la tabla "marca":
 * insertar, consultar, actualizar y eliminar (CRUD), usando JDBC puro
 * (sentencias {@link PreparedStatement} y try-with-resources).
 */
public class BrandDAO {

    /**
     * Inserta una nueva marca.
     *
     * @param brand marca a guardar (nombre y descripcion); el nombre
     *              debe ser unico segun la restriccion de la tabla
     * @return true si se inserto correctamente; false si ocurrio un
     *         error (por ejemplo, un nombre de marca repetido)
     */
    public boolean insertBrand(Brand brand) {

        String sql = "INSERT INTO marca (nombre, descripcion) VALUES (?, ?)";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, brand.getName());
            statement.setString(2, brand.getDescription());
            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("Error al insertar la marca.");
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Consulta todas las marcas registradas.
     *
     * @return lista de marcas (vacia si no hay registros o si
     *         ocurrio un error de conexion/consulta)
     */
    public List<Brand> getAllBrands() {

        List<Brand> brands = new ArrayList<>();

        String sql = "SELECT id_marca, nombre, descripcion FROM marca ORDER BY id_marca";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                brands.add(new Brand(
                        resultSet.getInt("id_marca"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion")
                ));
            }

        } catch (SQLException e) {

            System.out.println("Error al consultar las marcas.");
            e.printStackTrace();
        }

        return brands;
    }

    /**
     * Actualiza el nombre y la descripcion de una marca existente.
     *
     * @param brand marca con el id_marca a modificar y sus nuevos datos
     * @return true si se actualizo al menos una fila; false si no
     *         existia una marca con ese id o si ocurrio un error
     */
    public boolean updateBrand(Brand brand) {

        String sql = "UPDATE marca SET nombre = ?, descripcion = ? WHERE id_marca = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, brand.getName());
            statement.setString(2, brand.getDescription());
            statement.setInt(3, brand.getIdBrand());

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar la marca.");
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Elimina una marca por su identificador.
     * Nota: si la marca tiene productos asociados, la base de datos
     * rechazara la eliminacion por la llave foranea fk_producto_marca
     * (RESTRICT), y este metodo devolvera false porque la excepcion
     * SQLException sera capturada mas abajo.
     *
     * @param idBrand identificador de la marca a eliminar
     * @return true si se elimino al menos una fila; false si no
     *         existia, si tiene productos asociados, o si ocurrio
     *         otro error
     */
    public boolean deleteBrand(int idBrand) {

        String sql = "DELETE FROM marca WHERE id_marca = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, idBrand);

            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al eliminar la marca.");
            e.printStackTrace();

            return false;
        }
    }
}
