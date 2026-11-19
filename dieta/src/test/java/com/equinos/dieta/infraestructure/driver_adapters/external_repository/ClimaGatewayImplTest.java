package com.equinos.dieta.infraestructure.driver_adapters.external_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.model.ClimaDia;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/** Pruebas de ClimaGatewayImpl. RestTemplate y la respuesta de Open-Meteo son simulados. */
@ExtendWith(MockitoExtension.class)
class ClimaGatewayImplTest {

    private static final String URL_BASE = "http://clima.simulado";
    private static final String LATITUD = "1.5";
    private static final String LONGITUD = "-2.5";
    private static final String ZONA = "Zona";

    @Mock
    private RestTemplate restTemplate;

    private ClimaGatewayImpl gateway(boolean habilitado, long cacheMinutos) {
        return new ClimaGatewayImpl(restTemplate, habilitado, URL_BASE, LATITUD, LONGITUD, ZONA, cacheMinutos);
    }

    private static ClimaGatewayImpl.RespuestaClima respuestaSimulada(List<Double> temperaturas, List<Double> humedades) {
        ClimaGatewayImpl.Horas horas = mock(ClimaGatewayImpl.Horas.class);
        lenient().when(horas.temperaturas()).thenReturn(temperaturas);
        lenient().when(horas.humedades()).thenReturn(humedades);
        ClimaGatewayImpl.RespuestaClima respuesta = mock(ClimaGatewayImpl.RespuestaClima.class);
        lenient().when(respuesta.hourly()).thenReturn(horas);
        return respuesta;
    }

    private void apiResponde(ClimaGatewayImpl.RespuestaClima respuesta) {
        when(restTemplate.getForObject(anyString(), eq(ClimaGatewayImpl.RespuestaClima.class), eq(LATITUD), eq(LONGITUD)))
                .thenReturn(respuesta);
    }

    private void verificarLlamadas(int veces) {
        verify(restTemplate, times(veces))
                .getForObject(anyString(), eq(ClimaGatewayImpl.RespuestaClima.class), eq(LATITUD), eq(LONGITUD));
    }

    @Test
    void deshabilitadoNoConsultaLaApi() {
        //Act y Assert
        assertTrue(gateway(false, 60).climaDelDia().isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void devuelveLaHoraMasCalurosaDelDia() {
        //Arrange
        apiResponde(respuestaSimulada(List.of(18.0, 28.0), List.of(60.0, 80.0)));

        //Act
        ClimaDia clima = gateway(true, 60).climaDelDia().orElseThrow();

        //Assert
        assertEquals(ZONA, clima.zona());
        assertEquals(28.0, clima.temperaturaC());
        assertEquals(80, clima.humedad());
    }

    @Test
    void mientrasEstaVigenteNoVuelveAConsultar() {
        //Arrange
        apiResponde(respuestaSimulada(List.of(20.0), List.of(50.0)));
        ClimaGatewayImpl gateway = gateway(true, 60);

        //Act
        Optional<ClimaDia> primera = gateway.climaDelDia();
        Optional<ClimaDia> segunda = gateway.climaDelDia();

        //Assert
        assertEquals(primera, segunda);
        verificarLlamadas(1);
    }

    @Test
    void alVencerLaVigenciaConsultaDeNuevo() {
        //Arrange
        apiResponde(respuestaSimulada(List.of(20.0), List.of(50.0)));
        ClimaGatewayImpl gateway = gateway(true, 0);

        //Act
        gateway.climaDelDia();
        gateway.climaDelDia();

        //Assert
        verificarLlamadas(2);
    }

    @Test
    void siLaApiFallaNoHayClimaYEsperaAntesDeReintentar() {
        //Arrange
        when(restTemplate.getForObject(anyString(), eq(ClimaGatewayImpl.RespuestaClima.class), eq(LATITUD), eq(LONGITUD)))
                .thenThrow(new ResourceAccessException("sin conexión"));
        ClimaGatewayImpl gateway = gateway(true, 0);

        //Act y Assert
        assertTrue(gateway.climaDelDia().isEmpty());
        assertTrue(gateway.climaDelDia().isEmpty());
        verificarLlamadas(1);
    }

    @Test
    void unaRespuestaVaciaNoTieneClima() {
        //Arrange
        apiResponde(null);

        //Act y Assert
        assertTrue(gateway(true, 60).climaDelDia().isEmpty());
    }

    @Test
    void unaRespuestaSinHorasNoTieneClima() {
        //Arrange
        ClimaGatewayImpl.RespuestaClima respuesta = mock(ClimaGatewayImpl.RespuestaClima.class);
        apiResponde(respuesta);

        //Act y Assert
        assertTrue(gateway(true, 60).climaDelDia().isEmpty());
    }

    @Test
    void unaRespuestaSinTemperaturasNoTieneClima() {
        //Arrange
        apiResponde(respuestaSimulada(null, List.of(50.0)));

        //Act y Assert
        assertTrue(gateway(true, 60).climaDelDia().isEmpty());
    }

    @Test
    void unaRespuestaSinHumedadesNoTieneClima() {
        //Arrange
        apiResponde(respuestaSimulada(List.of(20.0), null));

        //Act y Assert
        assertTrue(gateway(true, 60).climaDelDia().isEmpty());
    }
}
