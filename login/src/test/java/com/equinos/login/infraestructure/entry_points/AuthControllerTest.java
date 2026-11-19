package com.equinos.login.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.model.SesionIniciada;
import com.equinos.login.domain.model.TokenEmitido;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.usecase.AutenticacionUseCase;
import com.equinos.login.infraestructure.security.UsuarioAutenticado;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de AuthController. El caso de uso, la petición, el usuario y el token son simulados. */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final Long ID = 1L;
    private static final Instant FECHA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private AutenticacionUseCase autenticacionUseCase;

    @InjectMocks
    private AuthController controller;

    private static Usuario usuarioSimulado() {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(ID);
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getCreadoEn()).thenReturn(FECHA);
        lenient().when(usuario.getActualizadoEn()).thenReturn(FECHA);
        return usuario;
    }

    private static SesionIniciada sesionSimulada(Usuario usuario) {
        TokenEmitido token = mock(TokenEmitido.class);
        lenient().when(token.token()).thenReturn("token-simulado");
        lenient().when(token.expiraEn()).thenReturn(FECHA);

        SesionIniciada sesion = mock(SesionIniciada.class);
        lenient().when(sesion.usuario()).thenReturn(usuario);
        lenient().when(sesion.token()).thenReturn(token);
        return sesion;
    }

    private static UsuarioAutenticado actualSimulado() {
        UsuarioAutenticado actual = mock(UsuarioAutenticado.class);
        lenient().when(actual.id()).thenReturn(ID);
        return actual;
    }

    @Test
    void loginDevuelveElTokenYLosDatosDelUsuario() {
        //Arrange
        LoginRequest request = mock(LoginRequest.class);
        lenient().when(request.correo()).thenReturn("ana@correo.com");
        lenient().when(request.password()).thenReturn("Clave12345");
        SesionIniciada sesion = sesionSimulada(usuarioSimulado());
        when(autenticacionUseCase.iniciarSesion("ana@correo.com", "Clave12345")).thenReturn(sesion);

        //Act
        TokenResponse respuesta = controller.login(request);

        //Assert
        assertEquals("token-simulado", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(FECHA, respuesta.expiraEn());
        assertEquals(ID, respuesta.usuario().id());
        assertEquals("ana@correo.com", respuesta.usuario().correo());
    }

    @Test
    void loginEnviaLosDatosDeLaPeticionAlCasoDeUso() {
        //Arrange
        LoginRequest request = new LoginRequest("ana@correo.com", "Clave12345");
        SesionIniciada sesion = sesionSimulada(usuarioSimulado());
        when(autenticacionUseCase.iniciarSesion("ana@correo.com", "Clave12345")).thenReturn(sesion);

        //Act
        controller.login(request);

        //Assert
        verify(autenticacionUseCase).iniciarSesion("ana@correo.com", "Clave12345");
    }

    @Test
    void loginConCredencialesInvalidasDejaPasarLaExcepcion() {
        //Arrange
        LoginRequest request = mock(LoginRequest.class);
        lenient().when(request.correo()).thenReturn("ana@correo.com");
        lenient().when(request.password()).thenReturn("mala");
        when(autenticacionUseCase.iniciarSesion("ana@correo.com", "mala"))
                .thenThrow(new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        //Act y Assert
        assertThrows(CredencialesInvalidasException.class, () -> controller.login(request));
    }

    @Test
    void renovarUsaElIdDelUsuarioAutenticado() {
        //Arrange
        SesionIniciada sesion = sesionSimulada(usuarioSimulado());
        when(autenticacionUseCase.renovar(ID)).thenReturn(sesion);

        //Act
        TokenResponse respuesta = controller.renovar(actualSimulado());

        //Assert
        assertEquals("token-simulado", respuesta.token());
        verify(autenticacionUseCase).renovar(ID);
    }

    @Test
    void perfilDevuelveLosDatosDelUsuarioAutenticado() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(autenticacionUseCase.perfil(ID)).thenReturn(usuario);

        //Act
        UsuarioResponse respuesta = controller.perfil(actualSimulado());

        //Assert
        assertEquals(ID, respuesta.id());
        assertEquals("Ana", respuesta.nombre());
        assertEquals("ana@correo.com", respuesta.correo());
        assertEquals(FECHA, respuesta.creadoEn());
        assertEquals(FECHA, respuesta.actualizadoEn());
    }
}
