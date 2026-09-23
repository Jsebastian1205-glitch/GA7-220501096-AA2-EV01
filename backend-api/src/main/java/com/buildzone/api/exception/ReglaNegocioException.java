package com.buildzone.api.exception;

/**
 * Se lanza cuando una peticion es valida en forma pero viola una regla
 * de negocio (por ejemplo, contrasena actual incorrecta o suscribirse a
 * un plan desactivado). Se traduce a HTTP 400.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
