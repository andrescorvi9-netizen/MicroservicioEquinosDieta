package com.equinos.login.domain.usecase;

import com.equinos.login.domain.exception.ConflictoException;
import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.RecursoNoEncontradoException;
import com.equinos.login.domain.exception.ReglaNegocioException;
import com.equinos.login.domain.model.RegistrarUsuarioCommand;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.CaballosGateway;
import com.equinos.login.domain.model.gateway.DietasGateway;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;
import com.equinos.login.domain.model.gateway.PasswordFiltradaGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private static final String NO_ENCONTRADO = "Usuario no encontrado";
    private static final String PASSWORD_FILTRADA =
            "Esta contraseña aparece en filtraciones de datos conocidas; elige una diferente";
    private static final int MAX_BYTES_PASSWORD = 72;

    private final UsuarioGateway usuarios;
    private final EncrypterGateway hasher;
    private final DietasGateway dietas;
    private final CaballosGateway caballos;
    private final PasswordFiltradaGateway passwordsFiltradas;
    private final NotificacionesGateway notificaciones;

    /** Sin transacción a propósito: la consulta a la API externa no debe retener una conexión a la base. */
    public Usuario registrar(RegistrarUsuarioCommand comando) {
        String correo = Usuario.normalizarCorreo(comando.correo());
        if (usuarios.existePorCorreo(correo)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        rechazarSiEsMuyLarga(comando.password());
        rechazarSiEstaFiltrada(comando.password());
        Usuario creado = usuarios.guardar(
                Usuario.nuevo(comando.nombre(), correo, hasher.hash(comando.password()), Instant.now()));
        notificaciones.bienvenida(creado);
        return creado;
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id, Long solicitanteId) {
        return cuentaPropia(id, solicitanteId);
    }

    @Transactional
    public Usuario actualizar(Long id, Long solicitanteId, String nombre, String correo) {
        Usuario usuario = cuentaPropia(id, solicitanteId);
        String nuevoCorreo = Usuario.normalizarCorreo(correo);
        if (!nuevoCorreo.equals(usuario.getCorreo()) && usuarios.existePorCorreo(nuevoCorreo)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        usuario.actualizarDatos(nombre, nuevoCorreo, Instant.now());
        return usuarios.guardar(usuario);
    }


    public void eliminar(Long id, Long solicitanteId) {
        if (!esCuentaPropiaExistente(id, solicitanteId)) {
            throw new RecursoNoEncontradoException(NO_ENCONTRADO);
        }
        dietas.eliminarDietasDeUsuario(id);
        caballos.desactivarCaballosDeUsuario(id);
        usuarios.eliminarPorId(id);
    }

    /** Sin transacción a propósito: la consulta a la API externa no debe retener una conexión a la base. */
    public void cambiarPassword(Long id, Long solicitanteId, String passwordActual, String passwordNueva) {
        Usuario usuario = cuentaPropia(id, solicitanteId);
        if (!hasher.coincide(passwordActual, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("La contraseña actual no es correcta");
        }
        if (passwordActual.equals(passwordNueva)) {
            throw new ReglaNegocioException("La contraseña nueva debe ser distinta de la actual");
        }
        rechazarSiEsMuyLarga(passwordNueva);
        rechazarSiEstaFiltrada(passwordNueva);
        usuario.cambiarPassword(hasher.hash(passwordNueva), Instant.now());
        usuarios.guardar(usuario);
        notificaciones.contrasenaCambiada(usuario);
    }

    @Transactional(readOnly = true)
    public boolean correoDisponible(String correo) {
        return !usuarios.existePorCorreo(Usuario.normalizarCorreo(correo));
    }


    /** BCrypt admite 72 bytes, y las tildes o la ñ ocupan más de un byte por carácter. */
    private static void rechazarSiEsMuyLarga(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new ReglaNegocioException("La contraseña es demasiado larga; usa menos caracteres o evita tildes y ñ");
        }
    }

    private void rechazarSiEstaFiltrada(String password) {
        if (passwordsFiltradas.estaFiltrada(password)) {
            throw new ReglaNegocioException(PASSWORD_FILTRADA);
        }
    }

    private Usuario cuentaPropia(Long id, Long solicitanteId) {
        if (id == null || !id.equals(solicitanteId)) {
            throw new RecursoNoEncontradoException(NO_ENCONTRADO);
        }
        return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADO));
    }


    private boolean esCuentaPropiaExistente(Long id, Long solicitanteId) {
        return id != null && id.equals(solicitanteId) && usuarios.existePorId(id);
    }
}
