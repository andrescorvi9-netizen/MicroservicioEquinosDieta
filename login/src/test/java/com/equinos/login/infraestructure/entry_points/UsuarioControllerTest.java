package com.equinos.login.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.exception.RecursoNoEncontradoException;
import com.equinos.login.domain.model.RegistrarUsuarioCommand;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.usecase.UsuarioUseCase;
import com.equinos.login.infraestructure.security.UsuarioAutenticado;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Pruebas de UsuarioController. El caso de uso, las peticiones y el usuario son simulados. */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final Long ID = 1L;
    private static final Instant FECHA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private UsuarioUseCase usuarioUseCase;

    @InjectMocks
    private UsuarioController controller;

    private static Usuario usuarioSimulado() {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(ID);
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getCreadoEn()).thenReturn(FECHA);
        lenient().when(usuario.getActualizadoEn()).thenReturn(FECHA);
        return usuario;
    }

    private static UsuarioAutenticado actualSimulado() {
        UsuarioAutenticado actual = mock(UsuarioAutenticado.class);
        lenient().when(actual.id()).thenReturn(ID);
        return actual;
    }

    @Test
    void registrarRespondeCreadoConLosDatosDelUsuario() {
        //Arrange
        RegistroUsuarioRequest request = mock(RegistroUsuarioRequest.class);
        lenient().when(request.nombre()).thenReturn("Ana");
        lenient().when(request.correo()).thenReturn("ana@correo.com");
        lenient().when(request.password()).thenReturn("Clave12345");
        Usuario usuario = usuarioSimulado();
        when(usuarioUseCase.registrar(any(RegistrarUsuarioCommand.class))).thenReturn(usuario);

        //Act
        ResponseEntity<UsuarioResponse> respuesta = controller.registrar(request);

        //Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(ID, respuesta.getBody().id());

        ArgumentCaptor<RegistrarUsuarioCommand> comando = ArgumentCaptor.forClass(RegistrarUsuarioCommand.class);
        verify(usuarioUseCase).registrar(comando.capture());
        assertEquals("Ana", comando.getValue().nombre());
        assertEquals("ana@correo.com", comando.getValue().correo());
        assertEquals("Clave12345", comando.getValue().password());
    }

    @Test
    void lasPeticionesEntreganSusDatosAlCasoDeUso() {
        //Arrange
        RegistroUsuarioRequest registro = new RegistroUsuarioRequest("Ana", "ana@correo.com", "Clave12345");
        ActualizarUsuarioRequest actualizacion = new ActualizarUsuarioRequest("Ana Maria", "anamaria@correo.com");
        CambioPasswordRequest cambio = new CambioPasswordRequest("Clave12345", "NuevaClave1");
        Usuario usuario = usuarioSimulado();
        UsuarioAutenticado actual = actualSimulado();
        when(usuarioUseCase.registrar(any(RegistrarUsuarioCommand.class))).thenReturn(usuario);
        when(usuarioUseCase.actualizar(ID, ID, "Ana Maria", "anamaria@correo.com")).thenReturn(usuario);

        //Act
        controller.registrar(registro);
        controller.actualizar(ID, actualizacion, actual);
        controller.cambiarPassword(ID, cambio, actual);

        //Assert
        ArgumentCaptor<RegistrarUsuarioCommand> comando = ArgumentCaptor.forClass(RegistrarUsuarioCommand.class);
        verify(usuarioUseCase).registrar(comando.capture());
        assertEquals("ana@correo.com", comando.getValue().correo());
        verify(usuarioUseCase).actualizar(ID, ID, "Ana Maria", "anamaria@correo.com");
        verify(usuarioUseCase).cambiarPassword(ID, ID, "Clave12345", "NuevaClave1");
    }

    @Test
    void obtenerDevuelveLaCuentaSolicitada() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarioUseCase.obtener(ID, ID)).thenReturn(usuario);

        //Act
        UsuarioResponse respuesta = controller.obtener(ID, actualSimulado());

        //Assert
        assertEquals(ID, respuesta.id());
        assertEquals("Ana", respuesta.nombre());
    }

    @Test
    void obtenerUnaCuentaAjenaDejaPasarLaExcepcion() {
        //Arrange
        UsuarioAutenticado actual = actualSimulado();
        when(usuarioUseCase.obtener(2L, ID)).thenThrow(new RecursoNoEncontradoException("Usuario no encontrado"));

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> controller.obtener(2L, actual));
    }

    @Test
    void actualizarEnviaLosDatosDeLaPeticion() {
        //Arrange
        ActualizarUsuarioRequest request = mock(ActualizarUsuarioRequest.class);
        lenient().when(request.nombre()).thenReturn("Ana Maria");
        lenient().when(request.correo()).thenReturn("anamaria@correo.com");
        Usuario usuario = usuarioSimulado();
        when(usuarioUseCase.actualizar(ID, ID, "Ana Maria", "anamaria@correo.com")).thenReturn(usuario);

        //Act
        UsuarioResponse respuesta = controller.actualizar(ID, request, actualSimulado());

        //Assert
        assertEquals(ID, respuesta.id());
        verify(usuarioUseCase).actualizar(ID, ID, "Ana Maria", "anamaria@correo.com");
    }

    @Test
    void eliminarRespondeSinContenido() {
        //Act
        ResponseEntity<Void> respuesta = controller.eliminar(ID, actualSimulado());

        //Assert
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(usuarioUseCase).eliminar(ID, ID);
    }

    @Test
    void eliminarUnaCuentaInexistenteDejaPasarLaExcepcion() {
        //Arrange
        UsuarioAutenticado actual = actualSimulado();
        doThrow(new RecursoNoEncontradoException("Usuario no encontrado")).when(usuarioUseCase).eliminar(ID, ID);

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> controller.eliminar(ID, actual));
    }

    @Test
    void cambiarPasswordRespondeSinContenido() {
        //Arrange
        CambioPasswordRequest request = mock(CambioPasswordRequest.class);
        lenient().when(request.passwordActual()).thenReturn("Clave12345");
        lenient().when(request.passwordNueva()).thenReturn("NuevaClave1");

        //Act
        ResponseEntity<Void> respuesta = controller.cambiarPassword(ID, request, actualSimulado());

        //Assert
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(usuarioUseCase).cambiarPassword(ID, ID, "Clave12345", "NuevaClave1");
    }

    @Test
    void disponibilidadIndicaQueElCorreoEstaLibreYLoNormaliza() {
        //Arrange
        when(usuarioUseCase.correoDisponible(" ANA@Correo.com ")).thenReturn(true);

        //Act
        DisponibilidadResponse respuesta = controller.disponibilidad(" ANA@Correo.com ");

        //Assert
        assertEquals("ana@correo.com", respuesta.correo());
        assertTrue(respuesta.disponible());
    }

    @Test
    void disponibilidadIndicaQueElCorreoEstaOcupado() {
        //Arrange
        when(usuarioUseCase.correoDisponible("ana@correo.com")).thenReturn(false);

        //Act
        DisponibilidadResponse respuesta = controller.disponibilidad("ana@correo.com");

        //Assert
        assertFalse(respuesta.disponible());
    }
}
