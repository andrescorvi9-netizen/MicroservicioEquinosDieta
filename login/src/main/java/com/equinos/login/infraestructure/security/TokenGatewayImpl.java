package com.equinos.login.infraestructure.security;

import com.equinos.login.domain.model.TokenEmitido;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.TokenGateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Implementación del gateway: emite el JWT firmado con HMAC. Lleva id (subject), nombre y correo. */
@Component
public class TokenGatewayImpl implements TokenGateway {

    private final JwtService jwtService;
    private final Duration vigencia;

    public TokenGatewayImpl(JwtService jwtService,
                           @Value("${app.jwt.expiration-minutes:60}") long minutosVigencia) {
        this.jwtService = jwtService;
        this.vigencia = Duration.ofMinutes(minutosVigencia);
    }

    @Override
    public TokenEmitido emitir(Usuario usuario, Instant ahora) {
        Instant expiraEn = ahora.plus(vigencia);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("correo", usuario.getCorreo())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiraEn))
                .signWith(jwtService.clave())
                .compact();
        return new TokenEmitido(token, expiraEn);
    }
}
