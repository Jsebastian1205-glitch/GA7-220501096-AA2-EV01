package com.buildzone.api.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.model.Categoria;
import com.buildzone.api.model.Marca;
import com.buildzone.api.model.Producto;
import com.buildzone.api.repository.CategoriaRepository;
import com.buildzone.api.repository.MarcaRepository;
import com.buildzone.api.repository.ProductoRepository;

/**
 * Perfiles "dev" y "prod": carga un catalogo de ejemplo (el mismo que el
 * front-end tenia escrito a mano en script.js) si la base no tiene
 * productos, para poder usar la aplicacion completa sin XAMPP y en la
 * demo desplegada en la nube. En el perfil por defecto (MySQL local) no
 * se ejecuta: se usan los datos reales.
 */
@Component
@Profile({"dev", "prod"})
@Order(2)
public class CatalogoDemoConfig implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogoDemoConfig.class);

    /** {categoria, marca, nombre, descripcion} */
    private static final String[][] PRODUCTOS = {
        {"Procesador", "Intel", "Core i5-14600K", "14 nucleos (6P+8E), hasta 5.3 GHz"},
        {"Procesador", "Intel", "Core i7-14700K", "20 nucleos (8P+12E), hasta 5.6 GHz"},
        {"Procesador", "Intel", "Core i9-14900K", "24 nucleos (8P+16E), hasta 6.0 GHz"},
        {"Procesador", "AMD", "Ryzen 5 7600X", "6 nucleos / 12 hilos, hasta 5.3 GHz"},
        {"Procesador", "AMD", "Ryzen 7 7700X", "8 nucleos / 16 hilos, hasta 5.4 GHz"},
        {"Tarjeta Grafica", "NVIDIA", "RTX 4060", "8 GB GDDR6, DLSS 3"},
        {"Tarjeta Grafica", "NVIDIA", "RTX 4070", "12 GB GDDR6X, DLSS 3"},
        {"Tarjeta Grafica", "NVIDIA", "RTX 4070 Ti", "12 GB GDDR6X, DLSS 3"},
        {"Tarjeta Grafica", "AMD", "Radeon RX 7600", "8 GB GDDR6, FSR 3"},
        {"Tarjeta Grafica", "AMD", "Radeon RX 7800 XT", "16 GB GDDR6, FSR 3"},
        {"Memoria RAM", "Kingston", "Fury Beast 16GB 3200MHz", "DDR4, CL16"},
        {"Memoria RAM", "Corsair", "Vengeance 32GB 6000MHz", "DDR5, CL36, 2x16 GB"},
        {"Memoria RAM", "G.Skill", "Trident Z5 32GB 6400MHz", "DDR5, CL32, 2x16 GB"},
        {"Memoria RAM", "Corsair", "Dominator Platinum 64GB 5600MHz", "DDR5, CL40, 2x32 GB"},
    };

    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CatalogoDemoConfig(MarcaRepository marcaRepository, CategoriaRepository categoriaRepository,
                              ProductoRepository productoRepository) {
        this.marcaRepository = marcaRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productoRepository.count() > 0) {
            return;
        }
        Map<String, Marca> marcas = new HashMap<>();
        Map<String, Categoria> categorias = new HashMap<>();

        for (String[] fila : PRODUCTOS) {
            Categoria categoria = categorias.computeIfAbsent(fila[0],
                    nombre -> categoriaRepository.save(new Categoria(nombre, null)));
            Marca marca = marcas.computeIfAbsent(fila[1],
                    nombre -> marcaRepository.save(new Marca(nombre, null)));

            Producto producto = new Producto();
            producto.setCategoria(categoria);
            producto.setMarca(marca);
            producto.setNombre(fila[2]);
            producto.setDescripcion(fila[3]);
            productoRepository.save(producto);
        }
        LOG.info("Catalogo de ejemplo cargado: {} productos.", PRODUCTOS.length);
    }
}
