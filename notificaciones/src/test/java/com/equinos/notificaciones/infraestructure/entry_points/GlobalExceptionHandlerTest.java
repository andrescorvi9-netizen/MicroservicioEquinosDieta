package com.equinos.notificaciones.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

/** Pruebas de GlobalExceptionHandler: cada excepción debe terminar en su código HTTP. */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String RUTA = "/api/internal/notificaciones";

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void rutaSimulada() {
        lenient().when(request.getRequestURI()).thenReturn(RUTA);
    }

    private static void verificar(ResponseEntity<ErrorApi> respuesta, HttpStatus status, String mensaje) {
        assertEquals(status, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(status.value(), respuesta.getBody().status());
        assertEquals(mensaje, respuesta.getBody().mensaje());
        assertEquals(RUTA, respuesta.getBody().ruta());
    }

    @Test
    void validacionDevuelveLosCamposOrdenados() {
        //Arrange
        FieldError errorNombre = mock(FieldError.class);
        lenient().when(errorNombre.getField()).thenReturn("nombre");
        lenient().when(errorNombre.getDefaultMessage()).thenReturn("es obligatorio");
        FieldError errorCorreo = mock(FieldError.class);
        lenient().when(errorCorreo.getField()).thenReturn("correo");
        lenient().when(errorCorreo.getDefaultMessage()).thenReturn("es obligatorio");
        BindingResult resultado = mock(BindingResult.class);
        lenient().when(resultado.getFieldErrors()).thenReturn(List.of(errorNombre, errorCorreo));
        MethodArgumentNotValidException error = mock(MethodArgumentNotValidException.class);
        lenient().when(error.getBindingResult()).thenReturn(resultado);

        //Act
        ResponseEntity<ErrorApi> respuesta = handler.validacion(error, request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
        assertEquals(List.of("correo: es obligatorio", "nombre: es obligatorio"), respuesta.getBody().detalles());
    }

    @Test
    void cuerpoIlegibleResponde400() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.cuerpoIlegible(mock(HttpMessageNotReadableException.class), request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
    }

    @Test
    void lasExcepcionesDeSpringConservanSuCodigo() {
        //Arrange
        HttpRequestMethodNotSupportedException error = mock(HttpRequestMethodNotSupportedException.class);
        lenient().when(error.getStatusCode()).thenReturn(HttpStatus.METHOD_NOT_ALLOWED);

        //Act
        ResponseEntity<ErrorApi> respuesta = handler.inesperado(error, request);

        //Assert
        verificar(respuesta, HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
    }

    @Test
    void unErrorInesperadoResponde500SinMostrarElDetalle() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.inesperado(new IllegalStateException("detalle interno"), request);

        //Assert
        verificar(respuesta, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio");
        assertTrue(respuesta.getBody().detalles().isEmpty());
    }
}
