package com.buildzone.api.repository;

import com.buildzone.api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@link Categoria}. Spring Data genera la
 * implementacion a partir del nombre de los metodos.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /**
     * @param nombre nombre a buscar
     * @return true si ya existe una categoria con ese nombre
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
