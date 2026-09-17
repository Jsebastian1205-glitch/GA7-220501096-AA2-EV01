-- =========================================================
-- BuildZone - Referencia de las tablas usadas por este modulo
-- Motor: MariaDB 10.4 (XAMPP) / MySQL 8+
--
-- NOTA: en el equipo de desarrollo la base de datos "buildzone"
-- ya existe con 10 tablas (categoria, comparacion, detalle_comparacion,
-- especificacion, fuente_precio, marca, noticia, precio, producto,
-- usuario). Este script NO se debe ejecutar sobre esa base: se deja
-- unicamente como referencia de las 3 tablas que usa el modulo de
-- gestion (producto, marca, categoria), confirmadas contra la
-- estructura real via phpMyAdmin.
-- =========================================================

CREATE DATABASE IF NOT EXISTS buildzone
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE buildzone;

CREATE TABLE IF NOT EXISTS marca (
    id_marca    INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    descripcion  TEXT
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    id_marca     INT NOT NULL,
    id_categoria INT NOT NULL,
    nombre       VARCHAR(150) NOT NULL,
    descripcion  TEXT,
    imagen       VARCHAR(255),
    CONSTRAINT fk_producto_marca
        FOREIGN KEY (id_marca) REFERENCES marca (id_marca),
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
);
