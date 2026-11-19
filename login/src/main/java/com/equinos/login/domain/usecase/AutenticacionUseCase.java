package com.equinos.login.domain.usecase;

import com.equinos.login.domain.exception.CredencialesInvalidasException;
import com.equinos.login.domain.exception.CuentaBloqueadaException;
import com.equinos.login.domain.model.SesionIniciada;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.TokenGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;

import java.time.Instant;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AutenticacionUseCase {

    private static final String CREDENCIALES = "Correo o contraseña incorrectos";

    private final UsuarioGateway usuarios;
    private final EncrypterGateway hasher;
    private final TokenGateway tokens;

    public SesionIniciada iniciarSesion(String correo, String password) {
        Instant ahora = Instant.now();
        Usuario usuario = usuarios.buscarPorCorreo(Usuario.normalizarCorreo(correo))
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES));

        if (usuario.estaBloqueado(ahora)) {
            throw new CuentaBloqueadaException(usuario.getBloqueadoHasta());
        }

        if (!hasher.coincide(password, usuario.getPasswordHash())) {
            boolean quedoBloqueada = usuario.registrarIntentoFallido(ahora);
            usuarios.guardar(usuario);
            if (quedoBloqueada) {
                throw new CredencialesInvalidasException(CREDENCIALES + ". La cuenta quedó bloqueada por "
                        + Usuario.DURACION_BLOQUEO.toMinutes() + " minutos");
            }
            throw new CredencialesInvalidasException(CREDENCIALES);
        }

        usuario.registrarIngresoExitoso(ahora);
        Usuario guardado = usuarios.guardar(usuario);
        return new SesionIniciada(guardado, tokens.emitir(guardado, ahora));
    }

    public SesionIniciada renovar(Long usuarioId) {
        Usuario usuario = perfil(usuarioId);
        return new SesionIniciada(usuario, tokens.emitir(usuario, Instant.now()));
    }

    public Usuario perfil(Long usuarioId) {
        return usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new CredencialesInvalidasException("La cuenta del token ya no existe"));
    }
}
