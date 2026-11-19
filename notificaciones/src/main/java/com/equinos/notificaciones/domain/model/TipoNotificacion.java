package com.equinos.notificaciones.domain.model;

/** Eventos del sistema que generan un correo. El asunto es fijo para cada tipo. */
public enum TipoNotificacion {
    BIENVENIDA("Bienvenido a DietaEquinos"),
    CONTRASENA_CAMBIADA("Tu contraseña de DietaEquinos cambió"),
    DIETA_GENERADA("Nueva dieta generada en DietaEquinos"),
    ESTADO_SALUD_CAMBIADO("Cambió el estado de salud de tu caballo");

    private final String asunto;

    TipoNotificacion(String asunto) {
        this.asunto = asunto;
    }

    public String asunto() {
        return asunto;
    }
}
