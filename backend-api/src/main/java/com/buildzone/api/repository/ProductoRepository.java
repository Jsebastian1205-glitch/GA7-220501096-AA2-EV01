package com.buildzone.api.repository;

import com.buildzone.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de {@link Producto}.
 */
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
