package com.equinos.registrocaballo.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

/** Pruebas de JwtService. El secreto se genera en cada ejecución: ningún valor queda escrito en el código. */
class JwtServiceTest {

    private final JwtService servicio = new JwtService(secretoAleatorio());

    private static String secretoAleatorio() {
        return UUID.randomUUID().toString() + UUID.randomUUID();
    }

    private static String token(String subject, Instant expiraEn, SecretKey clave) {
        return Jwts.builder()
                .subject(subject)
                .claim("nombre", "Ana")
                .claim("correo", "ana@correo.com")
                .expiration(Date.from(expiraEn))
                .signWith(clave)
                .compact();
    }

    @Test
    void leerDevuelveLosDatosDeUnTokenValido() {
        //Arrange
        Instant expiraEn = Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        String token = token("1", expiraEn, servicio.clave());

        //Act
        Optional<UsuarioAutenticado> usuario = servicio.leer(token);

        //Assert
        assertTrue(usuario.isPresent());
        assertEquals(1L, usuario.get().id());
        assertEquals("Ana", usuario.get().nombre());
        assertEquals("ana@correo.com", usuario.get().correo());
        assertEquals(expiraEn, usuario.get().expiraEn());
        assertEquals(token, usuario.get().token());
    }

    @Test
    void unTokenVencidoSeRechaza() {
        //Arrange
        String token = token("1", Instant.now().minusSeconds(3600), servicio.clave());

        //Act y Assert
        assertTrue(servicio.leer(token).isEmpty());
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        //Arrange
        JwtService otroServicio = new JwtService(secretoAleatorio());
        String token = token("1", Instant.now().plusSeconds(3600), otroServicio.clave());

        //Act y Assert
        assertTrue(servicio.leer(token).isEmpty());
    }

    @Test
    void unTextoQueNoEsTokenSeRechaza() {
        //Act y Assert
        assertTrue(servicio.leer("esto-no-es-un-token").isEmpty());
    }

    @Test
    void unTokenConIdNoNumericoSeRechaza() {
        //Arrange
        String token = token("abc", Instant.now().plusSeconds(3600), servicio.clave());

        //Act y Assert
        assertTrue(servicio.leer(token).isEmpty());
    }
}
