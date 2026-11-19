package com.equinos.dieta.infraestructure.driver_adapters.external_repository;

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

import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.Racion;
import com.equinos.dieta.infraestructure.security.UsuarioAutenticado;

import java.math.BigDecimal;

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

    private static Dieta dietaSimulada() {
        Racion racion = mock(Racion.class);
        lenient().when(racion.msTotalKg()).thenReturn(new BigDecimal("7.21"));
        lenient().when(racion.forrajeKg()).thenReturn(new BigDecimal("6.85"));
        lenient().when(racion.concentradoKg()).thenReturn(new BigDecimal("0.36"));
        Dieta dieta = mock(Dieta.class);
        lenient().when(dieta.caballoNombre()).thenReturn("Trueno");
        lenient().when(dieta.caballoCodigo()).thenReturn("EQ-001");
        lenient().when(dieta.racion()).thenReturn(racion);
        return dieta;
    }

    @Test
    void avisaAlUsuarioDeLaSesionConLaRacion() {
        //Arrange
        sesionCon(usuarioSimulado());
        ArgumentCaptor<HttpEntity<?>> peticion = ArgumentCaptor.forClass(HttpEntity.class);

        //Act
        gateway.dietaGenerada(dietaSimulada());

        //Assert
        verify(restTemplate).postForEntity(eq(RUTA), peticion.capture(), eq(Void.class));
        NotificacionesGatewayImpl.NotificacionRemota cuerpo =
                (NotificacionesGatewayImpl.NotificacionRemota) peticion.getValue().getBody();
        assertEquals(VALOR_INTERNO, peticion.getValue().getHeaders().getFirst("X-Internal-Key"));
        assertEquals("ana@correo.com", cuerpo.correo());
        assertEquals("DIETA_GENERADA", cuerpo.tipo());
        assertEquals("Trueno", cuerpo.datos().get("caballo"));
        assertEquals("7.21", cuerpo.datos().get("msTotalKg"));
    }

    @Test
    void sinSesionNoAvisa() {
        //Act
        gateway.dietaGenerada(dietaSimulada());

        //Assert
        verifyNoInteractions(restTemplate);
    }

    @Test
    void conOtroTipoDeUsuarioNoAvisa() {
        //Arrange
        sesionCon("anonimo");

        //Act
        gateway.dietaGenerada(dietaSimulada());

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
        assertDoesNotThrow(() -> gateway.dietaGenerada(dietaSimulada()));
    }
}
