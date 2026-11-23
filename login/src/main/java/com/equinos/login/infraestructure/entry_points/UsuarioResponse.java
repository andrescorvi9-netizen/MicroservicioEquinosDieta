package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.model.Usuario;

import java.time.OffsetDateTime;

/** Usuario sin hash */
public record UsuarioResponse(Long id, String nombre, String correo, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                HoraColombia.de(usuario.getCreadoEn()), HoraColombia.de(usuario.getActualizadoEn()));
    }
}
