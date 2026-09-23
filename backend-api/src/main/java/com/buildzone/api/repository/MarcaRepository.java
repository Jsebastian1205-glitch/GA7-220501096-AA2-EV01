package com.buildzone.api.repository;

import com.buildzone.api.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@link Marca}. Spring Data genera la implementacion
 * a partir del nombre de los metodos.
 */
public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    /**
     * @param nombre nombre a buscar
     * @return true si ya existe una marca con ese nombre (nombre es UNIQUE
     *         en la base de datos, esto evita depender solo del error SQL)
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
