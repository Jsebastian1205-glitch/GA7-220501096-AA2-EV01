package com.buildzone.api.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejador centralizado de errores para toda la API. Convierte cada tipo
 * de excepcion en una respuesta HTTP consistente ({@link ErrorResponse}),
 * en vez de dejar que Spring devuelva su pagina de error generica o una
 * traza interna.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de Bean Validation sobre los DTO de entrada: 400 con detalle por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        ErrorResponse cuerpo = new ErrorResponse(LocalDateTime.now(), 400, "Bad Request",
                "Los datos enviados no son validos.", request.getRequestURI(), detalles);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** JSON mal formado o con tipos incorrectos (p. ej. un rol inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es valido o tiene valores no permitidos.", request);
    }

    /** Parametro de ruta con tipo incorrecto, p. ej. /api/productos/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' no tiene un formato valido.", request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredenciales(CredencialesInvalidasException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccesoDenegadoException ex,
                                                               HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /** Autorizacion denegada por Spring Security dentro de un controlador. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta accion.", request);
    }

    /** Recurso (producto, marca, usuario, plan...) con un id que no existe: 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Valor unico repetido (nombre de marca, correo, username...): 409. */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> manejarDuplicado(RecursoDuplicadoException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Ultima linea de defensa para violaciones de integridad que la
     * validacion previa no alcanzo a detectar: por ejemplo, borrar una
     * Marca o Categoria que todavia tiene productos asociados.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex,
                                                           HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, "No se pudo completar la operacion: el registro esta "
                + "relacionado con otros datos (por ejemplo, una marca o categoria "
                + "que todavia tiene productos asociados).", request);
    }

    /** Ruta que no existe en la API: 404 (y no 500). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(NoResourceFoundException ex,
                                                                HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe.", request);
    }

    /** Verbo HTTP no soportado en una ruta existente: 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarMetodo(HttpRequestMethodNotSupportedException ex,
                                                       HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en esta ruta.", request);
    }

    /** Cualquier error no previsto: 500 sin exponer detalles internos al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex, HttpServletRequest request) {
        LOG.error("Error no controlado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Intenta de nuevo mas tarde.", request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, HttpServletRequest request) {
        ErrorResponse cuerpo = ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
