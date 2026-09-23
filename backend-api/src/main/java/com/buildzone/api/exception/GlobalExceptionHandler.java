package com.buildzone.api.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador centralizado de errores para toda la API. Convierte cada tipo
 * de excepcion en una respuesta HTTP consistente, en vez de dejar que
 * Spring devuelva su pagina de error generica.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Errores de validacion de Bean Validation (@NotBlank, @Size, etc.)
     * sobre los DTO de entrada. Devuelve 400 con un mapa campo -> mensaje.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    /**
     * Recurso no encontrado (Producto, Marca o Categoria con un id que no
     * existe). Devuelve 404.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Nombre duplicado en Marca o Categoria (violacion de la restriccion
     * UNIQUE detectada antes de tocar la base de datos). Devuelve 409.
     */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarDuplicado(RecursoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Ultima linea de defensa para violaciones de integridad que la
     * validacion previa no alcanzo a detectar: por ejemplo, borrar una
     * Marca o Categoria que todavia tiene productos asociados (viola la
     * llave foranea fk_producto_marca / fk_producto_categoria).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "No se pudo completar la operacion: el registro esta "
                        + "relacionado con otros datos (por ejemplo, una marca o categoria "
                        + "que todavia tiene productos asociados)."));
    }
}
