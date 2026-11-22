package com.equinos.login.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.model.Usuario;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de NotificacionesGatewayImpl. RestTemplate es simulado y el aviso corre en el mismo hilo. */
@ExtendWith(MockitoExtension.class)
class NotificacionesGatewayImplTest {

    private static final String URL_BASE = "http://notificaciones.simulado";
    private static final String RUTA = URL_BASE + "/api/internal/notificaciones";
    private static final String VALOR_INTERNO = "valor-simulado";

    @Mock
    private RestTemplate restTemplate;

    private NotificacionesGatewayImpl gateway;

    @BeforeEach
    void crearGateway() {
        gateway = new NotificacionesGatewayImpl(restTemplate, URL_BASE, VALOR_INTERNO, Runnable::run);
    }

    private static Usuario usuarioSimulado() {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        return usuario;
    }

    private HttpEntity<?> peticionEnviada() {
        ArgumentCaptor<HttpEntity<?>> peticion = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(RUTA), peticion.capture(), eq(Void.class));
        return peticion.getValue();
    }

    @Test
    void bienvenidaEnviaElTipoYLaClaveInterna() {
        //Act
        gateway.bienvenida(usuarioSimulado());

        //Assert
        HttpEntity<?> peticion = peticionEnviada();
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticion.getBody();
        assertEquals(VALOR_INTERNO, peticion.getHeaders().getFirst("X-Internal-Key"));
        assertEquals("ana@correo.com", cuerpo.correo());
        assertEquals("Ana", cuerpo.nombre());
        assertEquals("BIENVENIDA", cuerpo.tipo());
    }

    @Test
    void contrasenaCambiadaEnviaSuTipo() {
        //Act
        gateway.contrasenaCambiada(usuarioSimulado());

        //Assert
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticionEnviada().getBody();
        assertEquals("CONTRASENA_CAMBIADA", cuerpo.tipo());
    }

    @Test
    void siNotificacionesNoRespondeNoFallaLaOperacion() {
        //Arrange
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("sin conexión"));

        //Act y Assert
        assertDoesNotThrow(() -> gateway.bienvenida(usuarioSimulado()));
    }

    @Test
    void cuentaActualizadaSinCambioDeCorreoAvisaUnaVezConLosDatosNuevos() {
        //Act
        gateway.cuentaActualizada(usuarioSimulado(), "ana@correo.com");

        //Assert
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticionEnviada().getBody();
        assertEquals("CUENTA_ACTUALIZADA", cuerpo.tipo());
        assertEquals("ana@correo.com", cuerpo.correo());
        assertEquals(Map.of("nombre", "Ana", "correo", "ana@correo.com"), cuerpo.datos());
    }

    @Test
    void cuentaActualizadaConCambioDeCorreoAvisaTambienAlCorreoAnterior() {
        //Act
        gateway.cuentaActualizada(usuarioSimulado(), "anterior@correo.com");

        //Assert: un aviso al correo nuevo y otro al anterior, los dos con el correo anterior en los datos
        ArgumentCaptor<HttpEntity<?>> peticiones = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(2)).postForEntity(eq(RUTA), peticiones.capture(), eq(Void.class));
        List<String> destinatarios = peticiones.getAllValues().stream()
                .map(p -> ((NotificacionesGatewayImpl.NotificacionRemota) p.getBody()).correo()).toList();
        assertEquals(List.of("ana@correo.com", "anterior@correo.com"), destinatarios);
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticiones.getAllValues().get(1).getBody();
        assertEquals("anterior@correo.com", cuerpo.datos().get("correoAnterior"));
        assertEquals("ana@correo.com", cuerpo.datos().get("correo"));
    }
}
