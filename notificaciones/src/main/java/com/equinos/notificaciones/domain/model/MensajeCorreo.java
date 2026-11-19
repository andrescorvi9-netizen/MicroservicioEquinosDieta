package com.equinos.notificaciones.domain.model;

/** Correo de texto plano listo para enviarse. */
public record MensajeCorreo(String destinatario, String asunto, String cuerpo) {
}
