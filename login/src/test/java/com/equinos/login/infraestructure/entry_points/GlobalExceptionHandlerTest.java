package com.equinos.login.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.equinos.login.domain.exception.ConflictoException;
import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.CuentaBloqueadaException;
import com.equinos.login.domain.exception.RecursoNoEncontradoException;
import com.equinos.login.domain.exception.ReglaNegocioException;
import com.equinos.login.domain.exception.ServicioNoDisponibleException;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Pruebas de GlobalExceptionHandler: cada excepción debe terminar en su código HTTP. */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String RUTA = "/api/prueba";

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
        assertEquals(status.getReasonPhrase(), respuesta.getBody().error());
        assertEquals(mensaje, respuesta.getBody().mensaje());
        assertEquals(RUTA, respuesta.getBody().ruta());
    }

    @Test
    void recursoNoEncontradoResponde404() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.dominio(new RecursoNoEncontradoException("No existe"), request);

        //Assert
        verificar(respuesta, HttpStatus.NOT_FOUND, "No existe");
    }

    @Test
    void conflictoResponde409() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.dominio(new ConflictoException("Repetido"), request);

        //Assert
        verificar(respuesta, HttpStatus.CONFLICT, "Repetido");
    }

    @Test
    void servicioNoDisponibleResponde503() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.dominio(new ServicioNoDisponibleException("Caido"), request);

        //Assert
        verificar(respuesta, HttpStatus.SERVICE_UNAVAILABLE, "Caido");
    }

    @Test
    void reglaDeNegocioResponde400() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.dominio(new ReglaNegocioException("Regla"), request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "Regla");
    }

    @Test
    void credencialesInvalidasResponde401() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.credenciales(new CredencialesInvalidasException("Mal"), request);

        //Assert
        verificar(respuesta, HttpStatus.UNAUTHORIZED, "Mal");
    }

    @Test
    void cuentaBloqueadaResponde423() {
        //Arrange
        CuentaBloqueadaException error = new CuentaBloqueadaException(Instant.parse("2030-01-01T00:15:00Z"));

        //Act
        ResponseEntity<ErrorApi> respuesta = handler.cuentaBloqueada(error, request);

        //Assert
        verificar(respuesta, HttpStatus.LOCKED, error.getMessage());
    }

    @Test
    void validacionDevuelveLosCamposOrdenados() {
        //Arrange
        FieldError errorNombre = mock(FieldError.class);
        lenient().when(errorNombre.getField()).thenReturn("nombre");
        lenient().when(errorNombre.getDefaultMessage()).thenReturn("es obligatorio");
        FieldError errorCorreo = mock(FieldError.class);
        lenient().when(errorCorreo.getField()).thenReturn("correo");
        lenient().when(errorCorreo.getDefaultMessage()).thenReturn("no es un correo válido");
        BindingResult resultado = mock(BindingResult.class);
        lenient().when(resultado.getFieldErrors()).thenReturn(List.of(errorNombre, errorCorreo));
        MethodArgumentNotValidException error = mock(MethodArgumentNotValidException.class);
        lenient().when(error.getBindingResult()).thenReturn(resultado);

        //Act
        ResponseEntity<ErrorApi> respuesta = handler.validacion(error, request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
        assertEquals(List.of("correo: no es un correo válido", "nombre: es obligatorio"),
                respuesta.getBody().detalles());
    }

    @Test
    void cuerpoIlegibleResponde400() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.cuerpoIlegible(mock(HttpMessageNotReadableException.class), request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
    }

    @Test
    void parametroDeTipoInvalidoIndicaSuNombre() {
        //Arrange
        MethodArgumentTypeMismatchException error = mock(MethodArgumentTypeMismatchException.class);
        lenient().when(error.getName()).thenReturn("id");

        //Act
        ResponseEntity<ErrorApi> respuesta = handler.tipoInvalido(error, request);

        //Assert
        verificar(respuesta, HttpStatus.BAD_REQUEST, "El parámetro 'id' tiene un valor no válido");
    }

    @Test
    void violacionDeIntegridadResponde409() {
        //Act
        ResponseEntity<ErrorApi> respuesta = handler.integridad(mock(DataIntegrityViolationException.class), request);

        //Assert
        verificar(respuesta, HttpStatus.CONFLICT, "La operación choca con datos ya registrados");
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
