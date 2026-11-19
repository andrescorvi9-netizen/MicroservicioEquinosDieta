package com.equinos.notificaciones.infraestructure.entry_points;

import com.equinos.notificaciones.domain.model.Notificacion;
import com.equinos.notificaciones.domain.model.TipoNotificacion;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacionRequest(
        @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido")
        @Size(max = 160, message = "admite máximo 160 caracteres")
        String correo,
        @NotBlank(message = "es obligatorio") @Size(max = 120, message = "admite máximo 120 caracteres")
        String nombre,
        @NotNull(message = "es obligatorio")
        TipoNotificacion tipo,
        Map<String, String> datos) {

    public Notificacion aDominio() {
        return new Notificacion(correo, nombre, tipo, datos);
    }
}
