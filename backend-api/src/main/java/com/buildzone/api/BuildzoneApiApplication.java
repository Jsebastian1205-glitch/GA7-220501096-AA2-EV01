package com.buildzone.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la API REST del catalogo de BuildZone.
 * <p>
 * Expone servicios para administrar Producto, Marca y Categoria sobre la
 * misma base de datos que ya usa el modulo de consola JDBC de
 * {@code backend-java} (Proyecto Sena 1). Esta API no reemplaza ese
 * modulo: es una capa REST adicional e independiente sobre los mismos
 * datos, pensada para ser consumida por un front-end u otros clientes
 * HTTP en vez de por un menu de consola.
 */
@SpringBootApplication
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
