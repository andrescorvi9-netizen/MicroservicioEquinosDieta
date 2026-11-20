package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.notificaciones.domain.model.MensajeCorreo;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

/** Pruebas de CorreoGatewayImpl. JavaMailSender es simulado: no se conecta a Gmail. */
@ExtendWith(MockitoExtension.class)
class CorreoGatewayImplTest {

    private static final String REMITENTE = "remitente@correo.com";

    @Mock
    private JavaMailSender mailSender;

    private static MensajeCorreo mensajeSimulado() {
        MensajeCorreo mensaje = mock(MensajeCorreo.class);
        lenient().when(mensaje.destinatario()).thenReturn("ana@correo.com");
        lenient().when(mensaje.asunto()).thenReturn("Asunto");
        lenient().when(mensaje.cuerpo()).thenReturn("Cuerpo");
        return mensaje;
    }

    /** Mensaje MIME vacío, sin servidor: solo sirve para revisar lo que se habría enviado. */
    private static MimeMessage mimeVacio() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private static String contenidoCompleto(MimeMessage correo) throws Exception {
        correo.saveChanges();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        correo.writeTo(salida);
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void enviaElCorreoHtmlConSusDatosYElLogo() throws Exception {
        //Arrange
        MimeMessage correo = mimeVacio();
        when(mailSender.createMimeMessage()).thenReturn(correo);
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, true, REMITENTE);

        //Act
        gateway.enviar(mensajeSimulado());

        //Assert
        verify(mailSender).send(correo);
        assertEquals(REMITENTE, correo.getFrom()[0].toString());
        assertEquals("ana@correo.com", correo.getAllRecipients()[0].toString());
        assertEquals("Asunto", correo.getSubject());
        String contenido = contenidoCompleto(correo);
        assertTrue(contenido.contains("text/plain"), "debe llevar la versión de texto plano");
        assertTrue(contenido.contains("text/html"), "debe llevar la versión HTML");
        assertTrue(contenido.contains("Content-ID: <logo>"), "el logo debe ir adjunto en línea");
        assertTrue(contenido.contains("image/png"));
    }

    @Test
    void deshabilitadoNoSeConectaAGmail() {
        //Arrange
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, false, REMITENTE);

        //Act
        gateway.enviar(mensajeSimulado());

        //Assert
        verifyNoInteractions(mailSender);
    }

    @Test
    void unFalloDeEnvioNoSePropaga() {
        //Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeVacio());
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, true, REMITENTE);
        doThrow(new MailSendException("sin conexión")).when(mailSender).send(any(MimeMessage.class));

        //Act y Assert
        assertDoesNotThrow(() -> gateway.enviar(mensajeSimulado()));
    }

    @Test
    void unErrorAlArmarElCorreoNoSePropagaNiSeEnvia() throws Exception {
        //Arrange
        MimeMessage correo = mock(MimeMessage.class);
        doThrow(new MessagingException("dirección inválida")).when(correo).setFrom(any(Address.class));
        when(mailSender.createMimeMessage()).thenReturn(correo);
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, true, REMITENTE);

        //Act y Assert
        assertDoesNotThrow(() -> gateway.enviar(mensajeSimulado()));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
