package com.equinos.login.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.exception.ConflictoException;
import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.RecursoNoEncontradoException;
import com.equinos.login.domain.exception.ReglaNegocioException;
import com.equinos.login.domain.exception.ServicioNoDisponibleException;
import com.equinos.login.domain.model.RegistrarUsuarioCommand;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.CaballosGateway;
import com.equinos.login.domain.model.gateway.DietasGateway;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;
import com.equinos.login.domain.model.gateway.PasswordFiltradaGateway;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    private static final Long ID = 1L;
    private static final Long OTRO_ID = 2L;

    @Mock
    private UsuarioGateway usuarios;
    @Mock
    private EncrypterGateway hasher;
    @Mock
    private DietasGateway dietas;
    @Mock
    private CaballosGateway caballos;
    @Mock
    private PasswordFiltradaGateway passwordsFiltradas;
    @Mock
    private NotificacionesGateway notificaciones;

    @InjectMocks
    private UsuarioUseCase useCase;

    /** Usuario simulado con los datos que leen los casos de uso. */
    private static Usuario usuarioSimulado() {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(ID);
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getPasswordHash()).thenReturn("hash-actual");
        return usuario;
    }

    @Test
    void Registrar() {
        //Arrange
        RegistrarUsuarioCommand comando = mock(RegistrarUsuarioCommand.class);
        Usuario usuarioGuardado = mock(Usuario.class);

        lenient().when(comando.nombre()).thenReturn(" Ana ");
        lenient().when(comando.correo()).thenReturn(" ANA@Correo.com ");
        lenient().when(comando.password()).thenReturn("Clave12345");

        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(false);
        when(hasher.hash("Clave12345")).thenReturn("hash-nuevo");
        when(usuarios.guardar(any(Usuario.class))).thenReturn(usuarioGuardado);

        //Act
        Usuario usuarioResultado = useCase.registrar(comando);

        //Assert
        assertSame(usuarioGuardado, usuarioResultado);
        ArgumentCaptor<Usuario> enviado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).guardar(enviado.capture());
        assertEquals("Ana", enviado.getValue().getNombre());
        assertEquals("ana@correo.com", enviado.getValue().getCorreo());
        assertEquals("hash-nuevo", enviado.getValue().getPasswordHash());
        assertNull(enviado.getValue().getId());
    }

    @Test
    void RegistrarCorreoRepetido() {
        //Arrange
        RegistrarUsuarioCommand comando = mock(RegistrarUsuarioCommand.class);
        lenient().when(comando.correo()).thenReturn("ana@correo.com");
        when(usuarios.existePorCorreo("ana@correo.com")).thenReturn(true);

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.registrar(comando));
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(hasher);
    }

    @Test
    void ObtenerCuentaPropia() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));

        //Act
        Usuario usuarioResultado = useCase.obtener(ID, ID);

        //Assert
        assertSame(usuario, usuarioResultado);
    }

    @Test
    void CuentaAjena() {
        //Act y Assert: ni siquiera se consulta la base
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtener(ID, OTRO_ID));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtener(null, ID));
        verifyNoInteractions(usuarios);
    }

    @Test
    void CuentaNoExisteNoEncontrado() {
        //Arrange
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtener(ID, ID));
    }

    @Test
    void ActualizarNombreYCorreo() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(usuarios.existePorCorreo("anamaria@correo.com")).thenReturn(false);
        when(usuarios.guardar(usuario)).thenReturn(usuario);

        //Act
        Usuario usuarioResultado = useCase.actualizar(ID, ID, "Ana Maria", "AnaMaria@correo.com");

        //Assert
        assertSame(usuario, usuarioResultado);
        verify(usuario).actualizarDatos(eq("Ana Maria"), eq("anamaria@correo.com"), any(Instant.class));
    }

    @Test
    void ActualizarConservandoCorreo() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(usuarios.guardar(usuario)).thenReturn(usuario);

        //Act
        useCase.actualizar(ID, ID, "Ana Maria", " ANA@correo.com ");

        //Assert: es su mismo correo, no se pregunta si está repetido
        verify(usuarios, never()).existePorCorreo(anyString());
        verify(usuario).actualizarDatos(eq("Ana Maria"), eq("ana@correo.com"), any(Instant.class));
    }

    @Test
    void ActualizarCorreoConflicto() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(usuarios.existePorCorreo("luis@correo.com")).thenReturn(true);

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.actualizar(ID, ID, "Ana", "luis@correo.com"));
        verify(usuario, never()).actualizarDatos(anyString(), anyString(), any());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void EliminarDietasCaballosCuenta() {
        //Arrange
        when(usuarios.existePorId(ID)).thenReturn(true);

        //Act
        useCase.eliminar(ID, ID);

        //Assert: el orden de la cascada es dietas, caballos y al final la cuenta
        InOrder orden = inOrder(dietas, caballos, usuarios);
        orden.verify(dietas).eliminarDietasDeUsuario(ID);
        orden.verify(caballos).desactivarCaballosDeUsuario(ID);
        orden.verify(usuarios).eliminarPorId(ID);
    }

    @Test
    void EliminarUnaCuentaAjenaNo() {
        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.eliminar(ID, OTRO_ID));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.eliminar(null, ID));
        verifyNoInteractions(usuarios, dietas, caballos);
    }

    @Test
    void eliminarUnaCuentaInexistenteNoLlamaALosOtrosServicios() {
        //Arrange
        when(usuarios.existePorId(ID)).thenReturn(false);

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.eliminar(ID, ID));
        verifyNoInteractions(dietas, caballos);
        verify(usuarios, never()).eliminarPorId(anyLong());
    }

    @Test
    void siDietaNoRespondeNoSeTocanLosCaballosNiLaCuenta() {
        //Arrange
        when(usuarios.existePorId(ID)).thenReturn(true);
        doThrow(new ServicioNoDisponibleException("dieta no responde")).when(dietas).eliminarDietasDeUsuario(ID);

        //Act y Assert
        assertThrows(ServicioNoDisponibleException.class, () -> useCase.eliminar(ID, ID));
        verifyNoInteractions(caballos);
        verify(usuarios, never()).eliminarPorId(anyLong());
    }

    @Test
    void siRegistroCaballoNoRespondeLaCuentaNoSeElimina() {
        //Arrange
        when(usuarios.existePorId(ID)).thenReturn(true);
        doThrow(new ServicioNoDisponibleException("registro-caballo no responde"))
                .when(caballos).desactivarCaballosDeUsuario(ID);

        //Act y Assert
        assertThrows(ServicioNoDisponibleException.class, () -> useCase.eliminar(ID, ID));
        verify(dietas).eliminarDietasDeUsuario(ID);
        verify(usuarios, never()).eliminarPorId(anyLong());
    }

    @Test
    void CambiarPassword() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash-actual")).thenReturn(true);
        when(hasher.hash("NuevaClave1")).thenReturn("hash-nuevo");

        //Act
        useCase.cambiarPassword(ID, ID, "Clave12345", "NuevaClave1");

        //Assert
        verify(usuario).cambiarPassword(eq("hash-nuevo"), any(Instant.class));
        verify(usuarios).guardar(usuario);
    }

    @Test
    void cambiarPasswordConLaActualRechazado() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("equivocada", "hash-actual")).thenReturn(false);

        //Act y Assert
        assertThrows(CredencialesInvalidasException.class,
                () -> useCase.cambiarPassword(ID, ID, "equivocada", "NuevaClave1"));
        verify(usuario, never()).cambiarPassword(anyString(), any());
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void laContrasenaNuevaDebeSerDistintaDeLaActual() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash-actual")).thenReturn(true);

        //Act y Assert
        assertThrows(ReglaNegocioException.class,
                () -> useCase.cambiarPassword(ID, ID, "Clave12345", "Clave12345"));
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void RegistrarEnviaLaBienvenida() {
        //Arrange
        RegistrarUsuarioCommand comando = mock(RegistrarUsuarioCommand.class);
        Usuario usuarioGuardado = mock(Usuario.class);
        lenient().when(comando.nombre()).thenReturn("Ana");
        lenient().when(comando.correo()).thenReturn("ana@correo.com");
        lenient().when(comando.password()).thenReturn("Clave12345");
        when(hasher.hash("Clave12345")).thenReturn("hash-nuevo");
        when(usuarios.guardar(any(Usuario.class))).thenReturn(usuarioGuardado);

        //Act
        useCase.registrar(comando);

        //Assert
        verify(passwordsFiltradas).estaFiltrada("Clave12345");
        verify(notificaciones).bienvenida(usuarioGuardado);
    }

    @Test
    void RegistrarConPasswordFiltradaRechazado() {
        //Arrange
        RegistrarUsuarioCommand comando = mock(RegistrarUsuarioCommand.class);
        lenient().when(comando.correo()).thenReturn("ana@correo.com");
        lenient().when(comando.password()).thenReturn("Clave12345");
        when(passwordsFiltradas.estaFiltrada("Clave12345")).thenReturn(true);

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.registrar(comando));
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(hasher, notificaciones);
    }

    @Test
    void CambiarPasswordAvisaAlUsuario() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash-actual")).thenReturn(true);
        when(hasher.hash("NuevaClave1")).thenReturn("hash-nuevo");

        //Act
        useCase.cambiarPassword(ID, ID, "Clave12345", "NuevaClave1");

        //Assert
        verify(notificaciones).contrasenaCambiada(usuario);
    }

    @Test
    void CambiarPasswordFiltradaRechazado() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash-actual")).thenReturn(true);
        when(passwordsFiltradas.estaFiltrada("NuevaClave1")).thenReturn(true);

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.cambiarPassword(ID, ID, "Clave12345", "NuevaClave1"));
        verify(usuario, never()).cambiarPassword(anyString(), any());
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(notificaciones);
    }

    @Test
    void RegistrarConPasswordDeMasDe72BytesRechazado() {
        //Arrange
        RegistrarUsuarioCommand comando = mock(RegistrarUsuarioCommand.class);
        lenient().when(comando.correo()).thenReturn("ana@correo.com");
        lenient().when(comando.password()).thenReturn("ñ".repeat(40));

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.registrar(comando));
        verify(usuarios, never()).guardar(any());
        verifyNoInteractions(passwordsFiltradas, hasher, notificaciones);
    }

    @Test
    void CambiarPasswordDeMasDe72BytesRechazado() {
        //Arrange
        Usuario usuario = usuarioSimulado();
        when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));
        when(hasher.coincide("Clave12345", "hash-actual")).thenReturn(true);

        //Act y Assert
        assertThrows(ReglaNegocioException.class,
                () -> useCase.cambiarPassword(ID, ID, "Clave12345", "ñ".repeat(40)));
        verify(usuario, never()).cambiarPassword(anyString(), any());
        verifyNoInteractions(passwordsFiltradas, notificaciones);
    }
}
