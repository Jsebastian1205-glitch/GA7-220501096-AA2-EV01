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
 * Acceso a datos (DAO - Data Access Object) para la entidad Product.
 * <p>
 * Implementa las cuatro operaciones basicas de persistencia (CRUD) sobre
 * la tabla "producto" de la base de datos, usando JDBC puro: sentencias
 * {@link PreparedStatement} (para evitar inyeccion SQL) y bloques
 * try-with-resources (para que la conexion, el statement y el result set
 * se cierren automaticamente, incluso si ocurre un error).
 * <p>
 * Cada metodo abre su propia conexion a traves de
 * {@link DatabaseConnection#getConnection()} y la libera al terminar;
 * no se mantienen conexiones abiertas entre llamadas.
 */
public class ProductDAO {

    /**
     * Inserta un nuevo producto en la base de datos.
     * El id_producto no se envia: la columna es AUTO_INCREMENT y la
     * base de datos lo genera automaticamente.
     *
     * @param product objeto con los datos del producto a guardar
     *                (marca, categoria, nombre, descripcion e imagen)
     * @return true si la insercion afecto al menos una fila; false si
     *         ocurrio un error (por ejemplo, una marca o categoria
     *         inexistente que viola la llave foranea)
     */
    public boolean insertProduct(Product product) {

        // Sentencia parametrizada: los "?" se reemplazan mas abajo con
        // setInt/setString, nunca concatenando texto (evita inyeccion SQL).
        String sql = """
                INSERT INTO producto
                (id_marca, id_categoria, nombre, descripcion, imagen)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            // El orden de los setX debe coincidir con el orden de los "?"
            // en la sentencia SQL de arriba.
            statement.setInt(1, product.getIdBrand());
            statement.setInt(2, product.getIdCategory());
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getImage());

            statement.executeUpdate();

            return true;

        } catch (SQLException e) {

            // Se informa el error por consola y se reporta el fallo a
            // quien llamo al metodo (el menu decide como mostrarlo).
            System.out.println("Error al insertar el producto.");
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Consulta todos los productos registrados.
     *
     * @return lista con todos los productos (vacia si no hay registros
     *         o si ocurrio un error de conexion/consulta)
     */
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

            // Se recorre el ResultSet fila por fila y cada fila se
            // convierte en un objeto Product para no exponer JDBC
            // fuera de la capa DAO.
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

    /**
     * Busca un unico producto por su identificador.
     * Se usa, por ejemplo, para precargar los datos actuales antes de
     * mostrarlos al usuario en la opcion "Actualizar producto" del menu.
     *
     * @param idProduct identificador del producto (id_producto)
     * @return el producto encontrado, o null si no existe ningun
     *         producto con ese id (o si ocurrio un error de consulta)
     */
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

                // Como id_producto es llave primaria, a lo sumo hay una
                // fila: basta con comprobar si existe un primer registro.
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

    /**
     * Imprime en consola el listado de productos con el nombre de su
     * marca y categoria (en vez del id numerico), usando un INNER JOIN.
     * Pensado como reporte legible para el usuario final del menu.
     */
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

    /**
     * Actualiza los datos de un producto existente.
     * Se actualizan todos los campos a la vez, identificando la fila
     * por id_producto.
     *
     * @param product producto con el id_producto del registro a
     *                modificar y los nuevos valores de sus campos
     * @return true si se actualizo al menos una fila; false si no
     *         existia un producto con ese id o si ocurrio un error
     */
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

            // executeUpdate devuelve el numero de filas afectadas:
            // 0 significa que no existia una fila con ese id_producto.
            int rows = statement.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar el producto.");
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Elimina un producto por su identificador.
     *
     * @param idProduct identificador del producto a eliminar
     * @return true si se elimino al menos una fila; false si no
     *         existia un producto con ese id o si ocurrio un error
     */
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
