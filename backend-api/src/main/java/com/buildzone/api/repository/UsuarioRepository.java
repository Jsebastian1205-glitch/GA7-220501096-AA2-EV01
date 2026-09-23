package com.buildzone.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.buildzone.api.model.Usuario;

/**
 * Acceso a datos de {@link Usuario}. Las busquedas por correo y usuario
 * ignoran mayusculas para que "Ana@Mail.com" y "ana@mail.com" se traten
 * como la misma cuenta.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
