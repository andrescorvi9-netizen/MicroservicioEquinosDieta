package com.equinos.dieta.infraestructure.driver_adapters.external_repository;

import com.equinos.dieta.domain.exception.ServicioNoDisponibleException;
import com.equinos.dieta.domain.model.CaballoInfo;
import com.equinos.dieta.domain.model.gateway.CaballoGateway;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementación del gateway: consulta el caballo en registro-caballo (GET /api/caballos/{id}).
 * Reenvía el JWT del usuario, de modo que registro-caballo aplica su propia regla de propiedad:
 * si el caballo no existe, está borrado o es de otro usuario, responde 404.
 */
@Component
public class CaballoGatewayImpl implements CaballoGateway {

    private final RestTemplate restTemplate;
    private final String url;

    public CaballoGatewayImpl(RestTemplate restTemplate, @Value("${app.caballos.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Override
    public Optional<CaballoInfo> buscar(Long caballoId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + tokenActual());
            CaballoRemoto remoto = restTemplate.exchange(url + "/api/caballos/{id}", HttpMethod.GET,
                    new HttpEntity<>(headers), CaballoRemoto.class, caballoId).getBody();
            if (remoto == null) {
                return Optional.empty();
            }
            return Optional.of(new CaballoInfo(remoto.id(), remoto.nombre(), remoto.codigo(), remoto.pesoKg(),
                    remoto.categoria(), remoto.estadoSalud()));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException(
                    "No fue posible consultar el caballo en el servicio registro-caballo. Intente de nuevo más tarde");
        }
    }

    private static String tokenActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion == null ? "" : String.valueOf(autenticacion.getCredentials());
    }

    /** Campos que se leen de la respuesta de registro-caballo; el resto se ignora. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CaballoRemoto(Long id, String nombre, String codigo, BigDecimal pesoKg, String categoria,
                         String estadoSalud) {
    }
}
