package com.equinos.notificaciones.domain.usecase;

import com.equinos.notificaciones.domain.model.MensajeCorreo;
import com.equinos.notificaciones.domain.model.Notificacion;
import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;

import lombok.RequiredArgsConstructor;

/** Convierte cada evento del sistema en un correo para el usuario. */
@RequiredArgsConstructor
public class NotificacionUseCase {

    private static final String PIE = "\n\nEste es un mensaje automático de DietaEquinos; no es necesario responderlo.";

    private final CorreoGateway correo;

    public void enviar(Notificacion notificacion) {
        String cuerpo = "Hola " + notificacion.nombre() + ":\n\n" + contenido(notificacion) + PIE;
        correo.enviar(new MensajeCorreo(notificacion.correo(), notificacion.tipo().asunto(), cuerpo));
    }

    private static String contenido(Notificacion n) {
        return switch (n.tipo()) {
            case BIENVENIDA -> "Tu cuenta en DietaEquinos se creó correctamente. Ya puedes registrar tus caballos "
                    + "y generar sus dietas.";
            case CONTRASENA_CAMBIADA -> "La contraseña de tu cuenta se cambió correctamente. Si no fuiste tú, "
                    + "cambia tu contraseña lo antes posible.";
            case CUENTA_ACTUALIZADA -> "Los datos de tu cuenta en DietaEquinos se actualizaron.\n\nDatos actuales:\n"
                    + "- Nombre: " + n.dato("nombre") + "\n"
                    + "- Correo: " + n.dato("correo") + "\n"
                    + (n.datos().containsKey("correoAnterior") ? "- Correo anterior: " + n.dato("correoAnterior") + "\n" : "")
                    + "\nSi no fuiste tú, cambia tu contraseña lo antes posible.";
            case DIETA_GENERADA -> "Se generó una dieta para " + caballo(n) + ".\n\nRación diaria (materia seca):\n"
                    + "- Total: " + n.dato("msTotalKg") + " kg\n"
                    + "- Forraje: " + n.dato("forrajeKg") + " kg\n"
                    + "- Concentrado: " + n.dato("concentradoKg") + " kg\n\n"
                    + "Esta recomendación es orientativa y no sustituye la valoración de un veterinario.";
            case ESTADO_SALUD_CAMBIADO -> "El estado de salud de " + caballo(n) + " cambió a: " + n.dato("estadoSalud")
                    + ".\n\nGenera una dieta nueva para aplicar el perfil que corresponde a este estado.";
        };
    }

    private static String caballo(Notificacion n) {
        return n.dato("caballo") + " (" + n.dato("codigo") + ")";
    }
}
