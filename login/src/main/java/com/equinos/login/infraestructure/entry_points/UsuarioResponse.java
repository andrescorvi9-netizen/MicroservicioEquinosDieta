package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.model.Usuario;

import java.time.Instant;

/** Vista pública del usuario: nunca incluye el hash de la contraseña. */
public record UsuarioResponse(Long id, String nombre, String correo, Instant creadoEn, Instant actualizadoEn) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getCreadoEn(), usuario.getActualizadoEn());
    }
}
