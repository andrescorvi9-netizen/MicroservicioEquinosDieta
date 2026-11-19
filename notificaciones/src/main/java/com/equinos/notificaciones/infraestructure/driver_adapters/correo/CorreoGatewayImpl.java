package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import com.equinos.notificaciones.domain.model.MensajeCorreo;
import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envía el correo por SMTP con Spring Mail. Con app.correo.enabled=false no se conecta a Gmail.
 * Un fallo de envío solo se registra: una notificación nunca debe afectar la operación que la originó.
 */
@Component
public class CorreoGatewayImpl implements CorreoGateway {

    private static final Logger log = LoggerFactory.getLogger(CorreoGatewayImpl.class);

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remitente;

    public CorreoGatewayImpl(JavaMailSender mailSender, @Value("${app.correo.enabled}") boolean habilitado,
            @Value("${spring.mail.username}") String remitente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remitente = remitente;
    }

    @Override
    public void enviar(MensajeCorreo mensaje) {
        if (!habilitado) {
            log.info("Correo simulado (app.correo.enabled=false): {}", mensaje.asunto());
            return;
        }
        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setFrom(remitente);
        correo.setTo(mensaje.destinatario());
        correo.setSubject(mensaje.asunto());
        correo.setText(mensaje.cuerpo());
        try {
            mailSender.send(correo);
        } catch (MailException ex) {
            log.warn("No fue posible enviar el correo '{}': {}", mensaje.asunto(), ex.getMessage());
        }
    }
}
