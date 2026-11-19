package com.equinos.login.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.CuentaBloqueadaException;
import com.equinos.login.domain.model.SesionIniciada;
import com.equinos.login.domain.model.TokenEmitido;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.TokenGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas de AutenticacionUseCase. Los gateways, el usuario y el token son simulados con Mockito:
 * el usuario simulado responde si está bloqueado o si el intento fallido lo bloquea.
 */
@ExtendWith(MockitoExtension.class)
class AutenticacionUseCaseTest {

    private static final Long ID = 1L;

    @Mock
    private UsuarioGateway usuarios;
    @Mock
    private EncrypterGateway hasher;
    @Mock
    private TokenGateway tokens;

    @InjectMocks
    private AutenticacionUseCase useCase;

    /** Usuario simulado con correo, hash de la clave y si está bloqueado o no. */
    private static Usuario usuarioSimulado(boolean bloqueado) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(ID);
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getPasswordHash()).thenReturn("hash");
        lenient().when(usuario.estaBloqueado(any(Instant.class))).thenReturn(bloqueado);
        return usuario;
    }

    @Test
    void CredencialesCorrectasToken() {
        //Arrange
        Usuario usuario = usuarioSimulado(false);
        Usuario usuarioGuardado = mock(Usuario.class);
        TokenEmitido token = mock(TokenEmitido.class);

        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash")).thenReturn(true);
        when(usuarios.guardar(usuario)).thenReturn(usuarioGuardado);
        when(tokens.emitir(eq(usuarioGuardado), any(Instant.class))).thenReturn(token);

        //Act
        SesionIniciada sesion = useCase.iniciarSesion(" ANA@correo.com ", "Clave12345");

        //Assert
        assertSame(usuarioGuardado, sesion.usuario());
        assertSame(token, sesion.token());
        verify(usuario).registrarIngresoExitoso(any(Instant.class));
    }

    @Test
    void CorreoInexistenteInvalido() {
        //Arrange
        when(usuarios.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(CredencialesInvalidasException.class, () -> useCase.iniciarSesion("nadie@correo.com", "x"));
        verifyNoInteractions(hasher, tokens);
    }

    @Test
    void ContracenaInvalidaIntento() {
        //Arrange
        Usuario usuario = usuarioSimulado(false);
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(hasher.coincide("mala", "hash")).thenReturn(false);
        when(usuario.registrarIntentoFallido(any(Instant.class))).thenReturn(false);

        //Act
        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> useCase.iniciarSesion("ana@correo.com", "mala"));

        //Assert: se suma el intento, se guarda y no se emite token
        assertEquals("Correo o contraseña incorrectos", error.getMessage());
        verify(usuario).registrarIntentoFallido(any(Instant.class));
        verify(usuarios).guardar(usuario);
        verify(usuario, never()).registrarIngresoExitoso(any());
        verifyNoInteractions(tokens);
    }

    @Test
    void TercerIntentoFallidoBloqueo() {
        //Arrange: el usuario simulado avisa que este intento lo bloquea
        Usuario usuario = usuarioSimulado(false);
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(hasher.coincide("mala", "hash")).thenReturn(false);
        when(usuario.registrarIntentoFallido(any(Instant.class))).thenReturn(true);

        //Act
        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> useCase.iniciarSesion("ana@correo.com", "mala"));

        //Assert
        assertTrue(error.getMessage().contains("bloqueada por 15 minutos"));
        verify(usuarios).guardar(usuario);
        verifyNoInteractions(tokens);
    }

    @Test
    void BloqueadoNoIngresa() {
        //Arrange
        Instant hasta = Instant.parse("2030-01-01T00:15:00Z");
        Usuario usuario = usuarioSimulado(true);
        lenient().when(usuario.getBloqueadoHasta()).thenReturn(hasta);
        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));

        //Act
        CuentaBloqueadaException error = assertThrows(CuentaBloqueadaException.class,
                () -> useCase.iniciarSesion("ana@correo.com", "Clave12345"));

        //Assert: ni se revisa la clave
        assertEquals(hasta, error.getBloqueadoHasta());
        verifyNoInteractions(hasher, tokens);
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void BloqueoVencido() {
        //Arrange: tuvo un bloqueo, pero ya pasó, así que el usuario simulado no está bloqueado
        Usuario usuario = usuarioSimulado(false);
        lenient().when(usuario.getBloqueadoHasta()).thenReturn(Instant.parse("2020-01-01T00:00:00Z"));
        TokenEmitido token = mock(TokenEmitido.class);

        when(usuarios.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash")).thenReturn(true);
        when(usuarios.guardar(usuario)).thenReturn(usuario);
        when(tokens.emitir(eq(usuario), any(Instant.class))).thenReturn(token);

        //Act
        SesionIniciada sesion = useCase.iniciarSesion("ana@correo.com", "Clave12345");

        //Assert
        assertSame(token, sesion.token());
        verify(usuario).registrarIngresoExitoso(any(Instant.class));
    }

    @Test
    void RenovarTokenNuevo() {
        //Arrange
        Usuario usuario = usuarioSimulado(false);
        TokenEmitido token = mock(TokenEmitido.class);
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(tokens.emitir(eq(usuario), any(Instant.class))).thenReturn(token);

        //Act
        SesionIniciada sesion = useCase.renovar(ID);

        //Assert
        assertSame(usuario, sesion.usuario());
        assertSame(token, sesion.token());
    }

    @Test
    void perfilDevuelveLaCuentaDelToken() {
        //Arrange
        Usuario usuario = usuarioSimulado(false);
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));

        //Act
        Usuario usuarioResultado = useCase.perfil(ID);

        //Assert
        assertSame(usuario, usuarioResultado);
    }

    @Test
    void siLaCuentaDelTokenYaNoExisteSeRechaza() {
        //Arrange
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(CredencialesInvalidasException.class, () -> useCase.perfil(ID));
        assertThrows(CredencialesInvalidasException.class, () -> useCase.renovar(ID));
        verifyNoInteractions(tokens);
    }
}
