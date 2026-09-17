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
 * insertar, consultar, actualizar y eliminar (CRUD), usando JDBC.
 */
public class BrandDAO {

    // INSERTAR
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

    // CONSULTAR
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

    // ACTUALIZAR
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

    // ELIMINAR
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
