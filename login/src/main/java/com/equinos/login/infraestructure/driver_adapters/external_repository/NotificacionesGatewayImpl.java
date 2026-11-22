package com.equinos.login.infraestructure.driver_adapters.external_repository;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;

import java.util.HashMap;
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

    @Override
    public void cuentaActualizada(Usuario usuario, String correoAnterior) {
        Map<String, String> datos = new HashMap<>(Map.of("nombre", usuario.getNombre(), "correo", usuario.getCorreo()));
        boolean cambioCorreo = !usuario.getCorreo().equals(correoAnterior);
        if (cambioCorreo) {
            datos.put("correoAnterior", correoAnterior);
        }
        enviar(new NotificacionRemota(usuario.getCorreo(), usuario.getNombre(), "CUENTA_ACTUALIZADA", datos));
        if (cambioCorreo) {
            // El dueño real de la cuenta se entera aunque alguien haya cambiado el correo
            enviar(new NotificacionRemota(correoAnterior, usuario.getNombre(), "CUENTA_ACTUALIZADA", datos));
        }
    }

    private void avisar(Usuario usuario, String tipo) {
        enviar(new NotificacionRemota(usuario.getCorreo(), usuario.getNombre(), tipo, Map.of()));
    }

    private void enviar(NotificacionRemota notificacion) {
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
