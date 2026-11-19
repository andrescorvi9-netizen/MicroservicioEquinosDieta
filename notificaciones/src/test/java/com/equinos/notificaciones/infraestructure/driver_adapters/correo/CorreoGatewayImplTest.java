package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.equinos.notificaciones.domain.model.MensajeCorreo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
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

    @Test
    void enviaElCorreoConSusDatos() {
        //Arrange
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, true, REMITENTE);
        ArgumentCaptor<SimpleMailMessage> correo = ArgumentCaptor.forClass(SimpleMailMessage.class);

        //Act
        gateway.enviar(mensajeSimulado());

        //Assert
        verify(mailSender).send(correo.capture());
        assertEquals(REMITENTE, correo.getValue().getFrom());
        assertArrayEquals(new String[] {"ana@correo.com"}, correo.getValue().getTo());
        assertEquals("Asunto", correo.getValue().getSubject());
        assertEquals("Cuerpo", correo.getValue().getText());
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
        CorreoGatewayImpl gateway = new CorreoGatewayImpl(mailSender, true, REMITENTE);
        doThrow(new MailSendException("sin conexión")).when(mailSender).send(any(SimpleMailMessage.class));

        //Act y Assert
        assertDoesNotThrow(() -> gateway.enviar(mensajeSimulado()));
    }
}
