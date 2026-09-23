package com.buildzone.api.exception;

/**
 * El usuario esta autenticado pero no puede realizar la accion (cuenta
 * inactiva, o intenta modificar un recurso de otro usuario). HTTP 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
