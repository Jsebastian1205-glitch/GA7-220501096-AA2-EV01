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

    private DatabaseConnection() {
        // Clase de utilidad: no se debe instanciar.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
