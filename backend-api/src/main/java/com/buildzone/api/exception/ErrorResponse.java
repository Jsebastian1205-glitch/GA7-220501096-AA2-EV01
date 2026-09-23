package com.buildzone.api.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato unico de error de toda la API. El front-end (api.js) muestra
 * {@code detalles} si trae elementos, o si no {@code mensaje}.
 *
 * @param timestamp momento del error
 * @param status    codigo HTTP
 * @param error     descripcion corta del codigo HTTP
 * @param mensaje   mensaje legible para el usuario final
 * @param ruta      ruta solicitada
 * @param detalles  errores de validacion campo a campo (puede ir vacio)
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {

    public static ErrorResponse de(int status, String error, String mensaje, String ruta) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, ruta, List.of());
    }
}
