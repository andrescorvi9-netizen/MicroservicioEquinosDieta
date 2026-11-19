package com.equinos.login.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.model.TokenEmitido;
import com.equinos.login.domain.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de TokenGatewayImpl. JwtService y el usuario son simulados; la clave se genera en cada ejecución. */
@ExtendWith(MockitoExtension.class)
class TokenGatewayImplTest {

    @Mock
    private JwtService jwtService;

    @Test
    void emitirFirmaUnTokenConLosDatosDelUsuarioYSuVigencia() {
        //Arrange
        SecretKey clave = Keys.hmacShaKeyFor((UUID.randomUUID().toString() + UUID.randomUUID())
                .getBytes(StandardCharsets.UTF_8));
        when(jwtService.clave()).thenReturn(clave);
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(1L);
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        TokenGatewayImpl gateway = new TokenGatewayImpl(jwtService, 60);

        //Act
        TokenEmitido emitido = gateway.emitir(usuario, ahora);

        //Assert
        assertEquals(ahora.plus(Duration.ofMinutes(60)), emitido.expiraEn());
        Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(emitido.token()).getPayload();
        assertEquals("1", claims.getSubject());
        assertEquals("Ana", claims.get("nombre", String.class));
        assertEquals("ana@correo.com", claims.get("correo", String.class));
        assertEquals(emitido.expiraEn(), claims.getExpiration().toInstant());
    }
}
