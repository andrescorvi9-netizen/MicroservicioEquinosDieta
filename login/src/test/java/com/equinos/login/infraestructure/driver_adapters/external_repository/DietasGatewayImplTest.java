package com.equinos.login.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.exception.ServicioNoDisponibleException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de DietasGatewayImpl. RestTemplate es simulado: no se hace ninguna llamada HTTP real. */
@ExtendWith(MockitoExtension.class)
class DietasGatewayImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final String URL_BASE = "http://dietas.simulado";
    private static final String RUTA = URL_BASE + "/api/internal/dietas/usuario/{usuarioId}";
    private static final String VALOR_INTERNO = "valor-simulado";

    @Mock
    private RestTemplate restTemplate;

    private DietasGatewayImpl gateway;

    @BeforeEach
    void crearGateway() {
        gateway = new DietasGatewayImpl(restTemplate, URL_BASE, VALOR_INTERNO);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void eliminarLlamaADietaConElEncabezadoInterno() {
        //Act
        gateway.eliminarDietasDeUsuario(USUARIO_ID);

        //Assert
        ArgumentCaptor<HttpEntity> entidad = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(RUTA), eq(HttpMethod.DELETE), entidad.capture(), eq(Void.class),
                eq(USUARIO_ID));
        assertEquals(VALOR_INTERNO, entidad.getValue().getHeaders().getFirst("X-Internal-Key"));
    }

    @Test
    void siDietaNoRespondeSeAvisaQueLaCuentaNoSeElimino() {
        //Arrange
        when(restTemplate.exchange(eq(RUTA), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class),
                eq(USUARIO_ID))).thenThrow(new ResourceAccessException("sin conexion"));

        //Act
        ServicioNoDisponibleException error = assertThrows(ServicioNoDisponibleException.class,
                () -> gateway.eliminarDietasDeUsuario(USUARIO_ID));

        //Assert
        assertTrue(error.getMessage().contains("dieta"));
    }
}
