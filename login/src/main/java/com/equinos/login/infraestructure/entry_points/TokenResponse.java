package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.model.SesionIniciada;

import java.time.OffsetDateTime;

public record TokenResponse(String token, String tipo, OffsetDateTime expiraEn, UsuarioResponse usuario) {

    public static TokenResponse de(SesionIniciada sesion) {
        return new TokenResponse(sesion.token().token(), "Bearer", HoraColombia.de(sesion.token().expiraEn()),
                UsuarioResponse.de(sesion.usuario()));
    }
}
