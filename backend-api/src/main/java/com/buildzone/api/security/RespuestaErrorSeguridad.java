package com.buildzone.api.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.buildzone.api.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Escribe los errores 401 (sin sesion) y 403 (sin permisos) que produce
 * Spring Security con el mismo formato JSON ({@link ErrorResponse}) que
 * el resto de la API, para que el front-end los muestre igual.
 */
@Component
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RespuestaErrorSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Peticion sin token (o con token invalido/vencido) a una ruta protegida. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesion para realizar esta accion.", request.getRequestURI());
    }

    /** Usuario autenticado pero sin el rol requerido. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta accion.", request.getRequestURI());
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje, String ruta)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, ruta));
    }
}
