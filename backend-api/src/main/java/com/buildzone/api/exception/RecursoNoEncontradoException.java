package com.buildzone.api.exception;

/**
 * Se lanza cuando se pide, actualiza o elimina un recurso (Producto,
 * Marca o Categoria) que no existe en la base de datos. El
 * {@link GlobalExceptionHandler} la traduce a una respuesta HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
