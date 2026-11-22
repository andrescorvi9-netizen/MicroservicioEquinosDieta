package com.equinos.login.infraestructure.entry_points;

/** Respuesta de la consulta de disponibilidad: el correo normalizado y un mensaje legible. */
public record DisponibilidadResponse(String correo, String mensaje) {

    static final String LIBRE = "No existe una cuenta con ese correo";
    static final String OCUPADO = "Ya existe una cuenta con ese correo";

    public static DisponibilidadResponse de(String correo, boolean disponible) {
        return new DisponibilidadResponse(correo, disponible ? LIBRE : OCUPADO);
    }
}
