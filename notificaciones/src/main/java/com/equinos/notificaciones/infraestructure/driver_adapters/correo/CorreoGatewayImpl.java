package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import com.equinos.notificaciones.domain.model.MensajeCorreo;
import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Envía el correo por SMTP con Spring Mail. Con app.correo.enabled=false no se conecta a Gmail.
 * El correo lleva dos versiones: HTML con el banner y el logo, y texto plano para los clientes que no muestran HTML.
 * Un fallo de envío solo se registra: una notificación nunca debe afectar la operación que la originó.
 */
@Component
public class CorreoGatewayImpl implements CorreoGateway {

    private static final Logger log = LoggerFactory.getLogger(CorreoGatewayImpl.class);

    static final String RUTA_LOGO = "correo/logo.png";

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
        try {
            MimeMessage correo = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(correo, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(mensaje.destinatario());
            helper.setSubject(mensaje.asunto());
            helper.setText(mensaje.cuerpo(), PlantillaCorreoHtml.generar(mensaje.asunto(), mensaje.cuerpo()));
            helper.addInline(PlantillaCorreoHtml.CID_LOGO, new ClassPathResource(RUTA_LOGO), "image/png");
            mailSender.send(correo);
        } catch (MailException | MessagingException ex) {
            log.warn("No fue posible enviar el correo '{}': {}", mensaje.asunto(), ex.getMessage());
        }
    }
}
