package com.buildzone.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Encargada unicamente de abrir la conexion JDBC hacia la base de datos
 * "buildzone" en MySQL. Cada DAO solicita una conexion nueva por
 * operacion y la cierra con try-with-resources.
 *
 * Configurada para XAMPP: usuario "root" sin contrasena por defecto.
 * Si en su instalacion de XAMPP le puso contrasena al usuario root,
 * reemplace el valor de PASSWORD por esa contrasena.
 */
public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/buildzone?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    /**
     * Constructor privado: esta clase solo expone metodos estaticos
     * (es una clase de utilidad) y no tiene sentido crear instancias
     * de ella.
     */
    private DatabaseConnection() {
    }

    /**
     * Abre y devuelve una nueva conexion JDBC hacia la base de datos.
     * Quien invoque este metodo es responsable de cerrar la conexion
     * (idealmente con try-with-resources), tal como hacen los DAO de
     * este proyecto.
     *
     * @return una conexion JDBC lista para usar
     * @throws SQLException si el servidor MySQL no esta disponible,
     *                       las credenciales son incorrectas, o la
     *                       base de datos "buildzone" no existe
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
