package com.equinos.dieta.infraestructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Valida la firma y la vigencia de un JWT emitido por el servicio login.
 * Todos los servicios comparten el mismo secreto (variable de entorno JWT_SECRET, mínimo 32 caracteres).
 */
@Component
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${app.jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey clave() {
        return clave;
    }

    /** Devuelve el usuario del token, o vacío si el token es inválido, está alterado o venció. */
    public Optional<UsuarioAutenticado> leer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioAutenticado(
                    Long.valueOf(claims.getSubject()),
                    claims.get("nombre", String.class),
                    claims.get("correo", String.class),
                    claims.getExpiration().toInstant(),
                    token));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
