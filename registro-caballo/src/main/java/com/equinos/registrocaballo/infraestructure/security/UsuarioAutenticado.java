package com.equinos.registrocaballo.infraestructure.security;

import java.time.Instant;

/** Datos del usuario leídos del JWT. Es el "principal" disponible en los controladores. */
public record UsuarioAutenticado(Long id, String nombre, String correo, Instant expiraEn, String token) {
}
