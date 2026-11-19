package com.equinos.login.infraestructure.driver_adapters.external_repository;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;

import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Avisa al servicio notificaciones en segundo plano; si no responde, solo se registra en el log. */
@Component
public class NotificacionesGatewayImpl implements NotificacionesGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesGatewayImpl.class);

    private final RestTemplate restTemplate;
    private final String url;
    private final String claveInterna;
    private final Executor executor;

    public NotificacionesGatewayImpl(RestTemplate restTemplate, @Value("${app.notificaciones.url}") String url,
            @Value("${app.internal-key}") String claveInterna, Executor notificacionesExecutor) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.claveInterna = claveInterna;
        this.executor = notificacionesExecutor;
    }

    @Override
    public void bienvenida(Usuario usuario) {
        avisar(usuario, "BIENVENIDA");
    }

    @Override
    public void contrasenaCambiada(Usuario usuario) {
        avisar(usuario, "CONTRASENA_CAMBIADA");
    }

    private void avisar(Usuario usuario, String tipo) {
        NotificacionRemota notificacion = new NotificacionRemota(usuario.getCorreo(), usuario.getNombre(), tipo, Map.of());
        executor.execute(() -> publicar(notificacion));
    }

    private void publicar(NotificacionRemota notificacion) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", claveInterna);
        try {
            restTemplate.postForEntity(url + "/api/internal/notificaciones", new HttpEntity<>(notificacion, headers),
                    Void.class);
        } catch (RestClientException ex) {
            log.warn("No fue posible avisar al servicio notificaciones: {}", ex.getMessage());
        }
    }

    /** Cuerpo que recibe POST /api/internal/notificaciones. */
    record NotificacionRemota(String correo, String nombre, String tipo, Map<String, String> datos) {
    }
}
