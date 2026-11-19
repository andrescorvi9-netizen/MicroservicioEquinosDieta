package com.equinos.dieta.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.exception.ServicioNoDisponibleException;
import com.equinos.dieta.domain.model.CaballoInfo;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de CaballoGatewayImpl. RestTemplate y la sesión son simulados: no hay llamadas HTTP reales. */
@ExtendWith(MockitoExtension.class)
class CaballoGatewayImplTest {

    private static final Long CABALLO_ID = 10L;
    private static final String URL_BASE = "http://caballos.simulado";
    private static final String RUTA = URL_BASE + "/api/caballos/{id}";
    private static final BigDecimal PESO = new BigDecimal("500.00");

    @Mock
    private RestTemplate restTemplate;

    private CaballoGatewayImpl gateway;

    @BeforeEach
    void crearGateway() {
        gateway = new CaballoGatewayImpl(restTemplate, URL_BASE);
    }

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    private static void sesionConToken(String token) {
        Authentication autenticacion = mock(Authentication.class);
        lenient().when(autenticacion.getCredentials()).thenReturn(token);
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }

    private static CaballoGatewayImpl.CaballoRemoto caballoRemotoSimulado() {
        CaballoGatewayImpl.CaballoRemoto remoto = mock(CaballoGatewayImpl.CaballoRemoto.class);
        lenient().when(remoto.id()).thenReturn(CABALLO_ID);
        lenient().when(remoto.nombre()).thenReturn("Trueno");
        lenient().when(remoto.codigo()).thenReturn("EQ-001");
        lenient().when(remoto.pesoKg()).thenReturn(PESO);
        lenient().when(remoto.categoria()).thenReturn("adulto");
        lenient().when(remoto.estadoSalud()).thenReturn("laminitis");
        return remoto;
    }

    @SuppressWarnings("unchecked")
    private void registroCaballoResponde(CaballoGatewayImpl.CaballoRemoto cuerpo) {
        ResponseEntity<CaballoGatewayImpl.CaballoRemoto> respuesta = mock(ResponseEntity.class);
        lenient().when(respuesta.getBody()).thenReturn(cuerpo);
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(CaballoGatewayImpl.CaballoRemoto.class), eq(CABALLO_ID))).thenReturn(respuesta);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String encabezadoEnviado() {
        ArgumentCaptor<HttpEntity> entidad = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(RUTA), eq(HttpMethod.GET), entidad.capture(),
                eq(CaballoGatewayImpl.CaballoRemoto.class), eq(CABALLO_ID));
        return entidad.getValue().getHeaders().getFirst("Authorization");
    }

    @Test
    void buscarDevuelveElCaballoYReenviaElTokenDelUsuario() {
        //Arrange
        sesionConToken("token-simulado");
        registroCaballoResponde(caballoRemotoSimulado());

        //Act
        Optional<CaballoInfo> resultado = gateway.buscar(CABALLO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        assertEquals(CABALLO_ID, resultado.get().id());
        assertEquals("Trueno", resultado.get().nombre());
        assertEquals("EQ-001", resultado.get().codigo());
        assertEquals(PESO, resultado.get().pesoKg());
        assertEquals("adulto", resultado.get().categoria());
        assertEquals("laminitis", resultado.get().estadoSalud());
        assertEquals("Bearer token-simulado", encabezadoEnviado());
    }

    @Test
    void laRespuestaDeRegistroCaballoSeConvierteEnCaballoInfo() {
        //Arrange
        registroCaballoResponde(new CaballoGatewayImpl.CaballoRemoto(CABALLO_ID, "Trueno", "EQ-001", PESO, "adulto",
                "laminitis"));

        //Act
        Optional<CaballoInfo> resultado = gateway.buscar(CABALLO_ID);

        //Assert
        assertEquals(Optional.of(new CaballoInfo(CABALLO_ID, "Trueno", "EQ-001", PESO, "adulto", "laminitis")),
                resultado);
    }

    @Test
    void sinSesionSeEnviaElEncabezadoSinToken() {
        //Arrange
        registroCaballoResponde(caballoRemotoSimulado());

        //Act
        gateway.buscar(CABALLO_ID);

        //Assert
        assertEquals("Bearer ", encabezadoEnviado());
    }

    @Test
    void unaRespuestaSinCuerpoDevuelveVacio() {
        //Arrange
        registroCaballoResponde(null);

        //Act
        Optional<CaballoInfo> resultado = gateway.buscar(CABALLO_ID);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void unCaballoNoEncontradoDevuelveVacio() {
        //Arrange
        HttpClientErrorException noEncontrado = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                new HttpHeaders(), new byte[0], null);
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(CaballoGatewayImpl.CaballoRemoto.class), eq(CABALLO_ID))).thenThrow(noEncontrado);

        //Act
        Optional<CaballoInfo> resultado = gateway.buscar(CABALLO_ID);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void siRegistroCaballoNoRespondeSeAvisaQueNoEstaDisponible() {
        //Arrange
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(CaballoGatewayImpl.CaballoRemoto.class), eq(CABALLO_ID)))
                .thenThrow(new ResourceAccessException("sin conexion"));

        //Act
        ServicioNoDisponibleException error = assertThrows(ServicioNoDisponibleException.class,
                () -> gateway.buscar(CABALLO_ID));

        //Assert
        assertTrue(error.getMessage().contains("registro-caballo"));
    }
}
