package com.equinos.login.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de PasswordFiltradaGatewayImpl. RestTemplate es simulado: no hay llamadas HTTP reales. */
@ExtendWith(MockitoExtension.class)
class PasswordFiltradaGatewayImplTest {

    private static final String URL_BASE = "http://hibp.simulado";
    private static final String RUTA = URL_BASE + "/range/{prefijo}";
    /** SHA-1 de "password": 5BAA6 + 1E4C9B93F3F0682250B6CF8331B7EE68FD8. */
    private static final String PASSWORD = "password";
    private static final String PREFIJO = "5BAA6";
    private static final String SUFIJO = "1E4C9B93F3F0682250B6CF8331B7EE68FD8";

    @Mock
    private RestTemplate restTemplate;

    private PasswordFiltradaGatewayImpl gateway;

    @BeforeEach
    void crearGateway() {
        gateway = new PasswordFiltradaGatewayImpl(restTemplate, true, URL_BASE);
    }

    @SuppressWarnings("unchecked")
    private void apiResponde(String cuerpo) {
        ResponseEntity<String> respuesta = mock(ResponseEntity.class);
        lenient().when(respuesta.getBody()).thenReturn(cuerpo);
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class), eq(PREFIJO)))
                .thenReturn(respuesta);
    }

    @Test
    void deshabilitadaNoConsultaLaApi() {
        //Arrange
        PasswordFiltradaGatewayImpl apagado = new PasswordFiltradaGatewayImpl(restTemplate, false, URL_BASE);

        //Act y Assert
        assertFalse(apagado.estaFiltrada(PASSWORD));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void detectaUnaPasswordFiltrada() {
        //Arrange
        apiResponde("0018A45C4D1DEF81644B54AB7F969B88D65:1\r\n" + SUFIJO + ":52256179");

        //Act y Assert
        assertTrue(gateway.estaFiltrada(PASSWORD));
    }

    @Test
    void soloEnviaElPrefijoYPideRelleno() {
        //Arrange
        apiResponde(SUFIJO + ":3");
        ArgumentCaptor<HttpEntity<?>> peticion = ArgumentCaptor.forClass(HttpEntity.class);

        //Act
        gateway.estaFiltrada(PASSWORD);

        //Assert
        verify(restTemplate).exchange(eq(RUTA), eq(HttpMethod.GET), peticion.capture(), eq(String.class), eq(PREFIJO));
        assertEquals("true", peticion.getValue().getHeaders().getFirst("Add-Padding"));
    }

    @Test
    void unaPasswordQueNoApareceSePermite() {
        //Arrange
        apiResponde("0018A45C4D1DEF81644B54AB7F969B88D65:1");

        //Act y Assert
        assertFalse(gateway.estaFiltrada(PASSWORD));
    }

    @Test
    void lasLineasDeRellenoNoCuentan() {
        //Arrange
        apiResponde(SUFIJO + ":0");

        //Act y Assert
        assertFalse(gateway.estaFiltrada(PASSWORD));
    }

    @Test
    void unaLineaMalFormadaSeIgnora() {
        //Arrange
        apiResponde(SUFIJO);

        //Act y Assert
        assertFalse(gateway.estaFiltrada(PASSWORD));
    }

    @Test
    void unaRespuestaVaciaPermiteLaPassword() {
        //Arrange
        apiResponde(null);

        //Act y Assert
        assertFalse(gateway.estaFiltrada(PASSWORD));
    }

    @Test
    void siLaApiFallaLaPasswordSePermite() {
        //Arrange
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class), eq(PREFIJO)))
                .thenThrow(new ResourceAccessException("sin conexión"));

        //Act y Assert
        assertFalse(gateway.estaFiltrada(PASSWORD));
    }
}
