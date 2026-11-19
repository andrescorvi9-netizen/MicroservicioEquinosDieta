package com.equinos.login.infraestructure.driver_adapters.external_repository;

import com.equinos.login.domain.exception.ServicioNoDisponibleException;
import com.equinos.login.domain.model.gateway.CaballosGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Llamada interna a registro-caballo, para dar de baja los caballos del usuario, con el encabezado X-Internal-Key.
 * Es síncrona a propósito: la cuenta solo se borra si esta llamada salió bien.
 */
@Component
public class CaballosGatewayImpl implements CaballosGateway {

    private final RestTemplate restTemplate;
    private final String url;
    private final String claveInterna;

    public CaballosGatewayImpl(RestTemplate restTemplate, @Value("${app.caballos.url}") String url,
            @Value("${app.internal-key}") String claveInterna) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.claveInterna = claveInterna;
    }

    @Override
    public void desactivarCaballosDeUsuario(Long usuarioId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", claveInterna);
        try {
            restTemplate.exchange(url + "/api/internal/caballos/usuario/{usuarioId}",
                    HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, usuarioId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("No fue posible dar de baja los caballos de la cuenta: "
                    + "el servicio registro-caballo no respondió. La cuenta no se eliminó; intente de nuevo más tarde");
        }
    }
}
