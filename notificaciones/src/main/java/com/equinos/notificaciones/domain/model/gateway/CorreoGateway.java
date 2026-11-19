package com.equinos.notificaciones.domain.model.gateway;

import com.equinos.notificaciones.domain.model.MensajeCorreo;

public interface CorreoGateway {

    void enviar(MensajeCorreo mensaje);
}
