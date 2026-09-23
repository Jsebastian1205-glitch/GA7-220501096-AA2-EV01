package com.buildzone.api.dto;

import java.time.LocalDateTime;

import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.EstadoUsuario;
import com.buildzone.api.model.enums.Rol;

/** Datos publicos de un usuario. Nunca incluye el hash de la contrasena. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String username,
        String email,
        Rol rol,
        EstadoUsuario estado,
        LocalDateTime fechaRegistro) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getUsername(),
                u.getEmail(), u.getRol(), u.getEstado(), u.getFechaRegistro());
    }
}
