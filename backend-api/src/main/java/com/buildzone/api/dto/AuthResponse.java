package com.buildzone.api.dto;

/**
 * Respuesta de registro/login: token JWT y datos publicos del usuario.
 *
 * @param token   JWT que el cliente envia en "Authorization: Bearer ..."
 * @param tipo    siempre "Bearer"
 * @param usuario datos del usuario autenticado
 */
public record AuthResponse(String token, String tipo, UsuarioResponse usuario) {

    public static AuthResponse bearer(String token, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", usuario);
    }
}
