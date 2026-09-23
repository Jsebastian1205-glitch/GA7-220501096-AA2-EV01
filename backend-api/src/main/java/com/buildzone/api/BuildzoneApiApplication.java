package com.buildzone.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada de la API REST de BuildZone.
 * <p>
 * Modulos que expone:
 * <ul>
 *   <li><b>Catalogo</b>: Producto, Marca y Categoria, sobre las mismas tablas
 *       que usa el modulo de consola JDBC de {@code backend-java}.</li>
 *   <li><b>Seguridad y usuarios</b>: registro, inicio de sesion con JWT,
 *       perfil propio y administracion de usuarios por rol.</li>
 *   <li><b>Planes y suscripciones</b>: planes de pago y suscripciones de
 *       cada usuario.</li>
 * </ul>
 * Se excluye {@link UserDetailsServiceAutoConfiguration} porque la
 * autenticacion es propia (JWT + BCrypt en {@code AuthService}) y no se
 * quiere el usuario "user" con contrasena generada que Spring crea por
 * defecto.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BuildzoneApiApplication {

    /**
     * Arranca el contexto de Spring Boot.
     *
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        SpringApplication.run(BuildzoneApiApplication.class, args);
    }
}
