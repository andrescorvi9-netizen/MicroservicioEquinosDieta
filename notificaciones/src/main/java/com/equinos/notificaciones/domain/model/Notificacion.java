package com.equinos.notificaciones.domain.model;

import java.util.Map;

/** Solicitud de aviso a un usuario. Los datos varían según el tipo de notificación. */
public record Notificacion(String correo, String nombre, TipoNotificacion tipo, Map<String, String> datos) {

    public Notificacion {
        datos = datos == null ? Map.of() : Map.copyOf(datos);
    }

    public String dato(String clave) {
        return datos.getOrDefault(clave, "-");
    }
}
