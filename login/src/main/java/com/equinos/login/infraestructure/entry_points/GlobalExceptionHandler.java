package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.exception.ConflictoException;
import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.CuentaBloqueadaException;
import com.equinos.login.domain.exception.DominioException;
import com.equinos.login.domain.exception.RecursoNoEncontradoException;
import com.equinos.login.domain.exception.ServicioNoDisponibleException;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduce las excepciones a respuestas HTTP con el mismo formato en todo el servicio. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<ErrorApi> dominio(DominioException ex, HttpServletRequest request) {
        return respuesta(estadoDe(ex), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorApi> credenciales(DominioException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorApi> cuentaBloqueada(DominioException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.LOCKED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", request, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApi> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor no válido",
                request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorApi> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, "La operación choca con datos ya registrados", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception ex, HttpServletRequest request) {
        // Las excepciones propias de Spring MVC (ruta inexistente, método no permitido, parámetro faltante)
        // ya traen su código HTTP; se respeta en lugar de convertirlas en 500.
        if (ex instanceof org.springframework.web.ErrorResponse errorSpring) {
            HttpStatus status = HttpStatus.valueOf(errorSpring.getStatusCode().value());
            return respuesta(status, status.getReasonPhrase(), request, List.of());
        }
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio", request, List.of());
    }

    private HttpStatus estadoDe(DominioException ex) {
        if (ex instanceof RecursoNoEncontradoException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ConflictoException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof ServicioNoDisponibleException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ErrorApi> respuesta(HttpStatus status, String mensaje, HttpServletRequest request,
                                               List<String> detalles) {
        return ResponseEntity.status(status).body(ErrorApi.de(status, mensaje, request.getRequestURI(), detalles));
    }
}
