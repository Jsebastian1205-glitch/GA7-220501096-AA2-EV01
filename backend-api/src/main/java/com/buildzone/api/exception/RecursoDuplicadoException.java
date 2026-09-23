package com.buildzone.api.exception;

/**
 * Se lanza al intentar crear una Marca o Categoria con un nombre que ya
 * existe (nombre es UNIQUE en la base de datos). El
 * {@link GlobalExceptionHandler} la traduce a una respuesta HTTP 409
 * (Conflict).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
