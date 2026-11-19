package com.equinos.registrocaballo.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.infraestructure.security.UsuarioAutenticado;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de NotificacionesGatewayImpl. RestTemplate y la sesión son simulados. */
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

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    private static void sesionCon(Object principal) {
        Authentication sesion = mock(Authentication.class);
        lenient().when(sesion.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(sesion);
    }

    private static UsuarioAutenticado usuarioSimulado() {
        UsuarioAutenticado usuario = mock(UsuarioAutenticado.class);
        lenient().when(usuario.correo()).thenReturn("ana@correo.com");
        lenient().when(usuario.nombre()).thenReturn("Ana");
        return usuario;
    }

    private static Caballo caballoSimulado() {
        Caballo caballo = mock(Caballo.class);
        lenient().when(caballo.getNombre()).thenReturn("Trueno");
        lenient().when(caballo.getCodigo()).thenReturn("EQ-001");
        lenient().when(caballo.getEstadoSalud()).thenReturn(EstadoSalud.COLICOS);
        return caballo;
    }

    @Test
    void avisaAlUsuarioDeLaSesionConElNuevoEstado() {
        //Arrange
        sesionCon(usuarioSimulado());
        ArgumentCaptor<HttpEntity<?>> peticion = ArgumentCaptor.forClass(HttpEntity.class);

        //Act
        gateway.estadoSaludCambiado(caballoSimulado());

        //Assert
        verify(restTemplate).postForEntity(eq(RUTA), peticion.capture(), eq(Void.class));
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticion.getValue().getBody();
        assertEquals(VALOR_INTERNO, peticion.getValue().getHeaders().getFirst("X-Internal-Key"));
        assertEquals("ana@correo.com", cuerpo.correo());
        assertEquals("ESTADO_SALUD_CAMBIADO", cuerpo.tipo());
        assertEquals("colicos", cuerpo.datos().get("estadoSalud"));
    }

    @Test
    void sinSesionNoAvisa() {
        //Act
        gateway.estadoSaludCambiado(caballoSimulado());

        //Assert
        verifyNoInteractions(restTemplate);
    }

    @Test
    void conOtroTipoDeUsuarioNoAvisa() {
        //Arrange
        sesionCon("anonimo");

        //Act
        gateway.estadoSaludCambiado(caballoSimulado());

        //Assert
        verifyNoInteractions(restTemplate);
    }

    @Test
    void siNotificacionesNoRespondeNoFallaLaOperacion() {
        //Arrange
        sesionCon(usuarioSimulado());
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("sin conexión"));

        //Act y Assert
        assertDoesNotThrow(() -> gateway.estadoSaludCambiado(caballoSimulado()));
    }
}
