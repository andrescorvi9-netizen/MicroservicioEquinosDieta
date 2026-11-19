package com.equinos.login.infraestructure.driver_adapters.external_repository;

import com.equinos.login.domain.exception.ServicioNoDisponibleException;
import com.equinos.login.domain.model.gateway.DietasGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Llamada interna a dieta, para borrar las dietas del usuario, con el encabezado X-Internal-Key.
 * Es síncrona a propósito: la cuenta solo se borra si esta llamada salió bien.
 */
@Component
public class DietasGatewayImpl implements DietasGateway {

    private final RestTemplate restTemplate;
    private final String url;
    private final String claveInterna;

    public DietasGatewayImpl(RestTemplate restTemplate, @Value("${app.dietas.url}") String url,
            @Value("${app.internal-key}") String claveInterna) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.claveInterna = claveInterna;
    }

    @Override
    public void eliminarDietasDeUsuario(Long usuarioId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", claveInterna);
        try {
            restTemplate.exchange(url + "/api/internal/dietas/usuario/{usuarioId}",
                    HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, usuarioId);
        } catch (RestClientException ex) {
            throw new ServicioNoDisponibleException("No fue posible eliminar las dietas de la cuenta: "
                    + "el servicio dieta no respondió. La cuenta no se eliminó; intente de nuevo más tarde");
        }
    }
}
