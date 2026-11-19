package com.equinos.login.infraestructure.driver_adapters.external_repository;

import com.equinos.login.domain.model.gateway.PasswordFiltradaGateway;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Consulta la API Pwned Passwords de Have I Been Pwned con k-anonimato: solo salen del servidor los 5 primeros
 * caracteres del hash SHA-1 y la comparación final se hace aquí. Si la API falla, la contraseña se permite.
 */
@Component
public class PasswordFiltradaGatewayImpl implements PasswordFiltradaGateway {

    private static final Logger log = LoggerFactory.getLogger(PasswordFiltradaGatewayImpl.class);
    private static final int LARGO_PREFIJO = 5;

    private final RestTemplate restTemplate;
    private final boolean habilitado;
    private final String url;

    public PasswordFiltradaGatewayImpl(RestTemplate restTemplate, @Value("${app.hibp.enabled}") boolean habilitado,
            @Value("${app.hibp.url}") String url) {
        this.restTemplate = restTemplate;
        this.habilitado = habilitado;
        this.url = url;
    }

    @Override
    public boolean estaFiltrada(String password) {
        if (!habilitado) {
            return false;
        }
        String hash = sha1(password);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Add-Padding", "true"); // la respuesta siempre pesa parecido: no revela el prefijo consultado
        try {
            String cuerpo = restTemplate.exchange(url + "/range/{prefijo}", HttpMethod.GET, new HttpEntity<>(headers),
                    String.class, hash.substring(0, LARGO_PREFIJO)).getBody();
            return cuerpo != null && contiene(cuerpo, hash.substring(LARGO_PREFIJO));
        } catch (RestClientException ex) {
            log.warn("Pwned Passwords no respondió; se omite la verificación: {}", ex.getMessage());
            return false;
        }
    }

    /** Cada línea es SUFIJO:VECES. Las líneas de relleno traen 0 veces y no cuentan. */
    private static boolean contiene(String cuerpo, String sufijo) {
        return cuerpo.lines().map(linea -> linea.trim().split(":")).anyMatch(partes -> partes.length == 2
                && partes[0].equalsIgnoreCase(sufijo) && !"0".equals(partes[1]));
    }

    private static String sha1(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 no disponible", ex);
        }
    }
}
