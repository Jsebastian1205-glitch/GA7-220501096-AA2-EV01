package com.buildzone.api.security;

import com.buildzone.api.model.enums.Rol;

/**
 * Identidad del usuario que hace la peticion, tal como la deja el
 * {@link JwtAuthenticationFilter} en el contexto de seguridad. Los
 * controladores la reciben con {@code @AuthenticationPrincipal}.
 *
 * @param id       id del usuario
 * @param username nombre de usuario
 * @param rol      rol vigente (leido de la base, no solo del token)
 */
public record UsuarioAutenticado(Long id, String username, Rol rol) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
