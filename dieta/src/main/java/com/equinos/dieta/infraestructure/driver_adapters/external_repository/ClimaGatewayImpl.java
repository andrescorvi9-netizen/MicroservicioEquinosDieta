package com.equinos.dieta.infraestructure.driver_adapters.external_repository;

import com.equinos.dieta.domain.model.ClimaDia;
import com.equinos.dieta.domain.model.gateway.ClimaGateway;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Consulta la API Open-Meteo (gratuita, sin clave) para un punto fijo de referencia. El resultado se guarda en
 * memoria: miles de dietas generan una sola llamada por hora. Si la API falla, se reintenta a los pocos minutos.
 */
@Component
public class ClimaGatewayImpl implements ClimaGateway {

    private static final Logger log = LoggerFactory.getLogger(ClimaGatewayImpl.class);
    private static final Duration ESPERA_TRAS_FALLO = Duration.ofMinutes(5);
    private static final String RUTA = "/v1/forecast?latitude={latitud}&longitude={longitud}"
            + "&hourly=temperature_2m,relative_humidity_2m&forecast_days=1&timezone=auto";

    private final RestTemplate restTemplate;
    private final boolean habilitado;
    private final String url;
    private final String latitud;
    private final String longitud;
    private final String zona;
    private final Duration vigencia;

    private Optional<ClimaDia> ultimo = Optional.empty();
    private Instant venceEn = Instant.MIN;

    public ClimaGatewayImpl(RestTemplate restTemplate, @Value("${app.clima.enabled}") boolean habilitado,
            @Value("${app.clima.url}") String url, @Value("${app.clima.latitud}") String latitud,
            @Value("${app.clima.longitud}") String longitud, @Value("${app.clima.nombre-zona}") String zona,
            @Value("${app.clima.cache-minutos}") long cacheMinutos) {
        this.restTemplate = restTemplate;
        this.habilitado = habilitado;
        this.url = url;
        this.latitud = latitud;
        this.longitud = longitud;
        this.zona = zona;
        this.vigencia = Duration.ofMinutes(cacheMinutos);
    }

    /** Sincronizado para que, al vencer la memoria, solo una petición consulte la API. */
    @Override
    public synchronized Optional<ClimaDia> climaDelDia() {
        if (!habilitado) {
            return Optional.empty();
        }
        Instant ahora = Instant.now();
        if (ahora.isBefore(venceEn)) {
            return ultimo;
        }
        ultimo = consultar();
        venceEn = ahora.plus(ultimo.isPresent() ? vigencia : ESPERA_TRAS_FALLO);
        return ultimo;
    }

    private Optional<ClimaDia> consultar() {
        try {
            RespuestaClima respuesta = restTemplate.getForObject(url + RUTA, RespuestaClima.class, latitud, longitud);
            if (respuesta == null || respuesta.hourly() == null || respuesta.hourly().temperaturas() == null
                    || respuesta.hourly().humedades() == null) {
                return Optional.empty();
            }
            return ClimaDia.deLaHoraMasCalurosa(zona, respuesta.hourly().temperaturas(), respuesta.hourly().humedades());
        } catch (RestClientException ex) {
            log.warn("Open-Meteo no respondió; la dieta se genera sin aviso de clima: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /** Campos que se leen de la respuesta de Open-Meteo; el resto se ignora. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaClima(Horas hourly) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Horas(@JsonProperty("temperature_2m") List<Double> temperaturas,
                 @JsonProperty("relative_humidity_2m") List<Double> humedades) {
    }
}
