package com.equinos.notificaciones.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.equinos.notificaciones.domain.model.MensajeCorreo;
import com.equinos.notificaciones.domain.model.Notificacion;
import com.equinos.notificaciones.domain.model.TipoNotificacion;
import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de NotificacionUseCase. El envío de correo es simulado. */
@ExtendWith(MockitoExtension.class)
class NotificacionUseCaseTest {

    private static final String CORREO = "ana@correo.com";

    @Mock
    private CorreoGateway correo;

    @InjectMocks
    private NotificacionUseCase useCase;

    private MensajeCorreo mensajeDe(TipoNotificacion tipo, Map<String, String> datos) {
        useCase.enviar(new Notificacion(CORREO, "Ana", tipo, datos));
        ArgumentCaptor<MensajeCorreo> mensaje = ArgumentCaptor.forClass(MensajeCorreo.class);
        verify(correo).enviar(mensaje.capture());
        return mensaje.getValue();
    }

    @Test
    void bienvenidaSaludaAlUsuario() {
        //Act
        MensajeCorreo mensaje = mensajeDe(TipoNotificacion.BIENVENIDA, Map.of());

        //Assert
        assertEquals(CORREO, mensaje.destinatario());
        assertEquals(TipoNotificacion.BIENVENIDA.asunto(), mensaje.asunto());
        assertTrue(mensaje.cuerpo().startsWith("Hola Ana:"));
        assertTrue(mensaje.cuerpo().contains("se creó correctamente"));
    }

    @Test
    void contrasenaCambiadaAlertaAlUsuario() {
        //Act
        MensajeCorreo mensaje = mensajeDe(TipoNotificacion.CONTRASENA_CAMBIADA, Map.of());

        //Assert
        assertTrue(mensaje.cuerpo().contains("Si no fuiste tú"));
    }

    @Test
    void dietaGeneradaIncluyeLaRacion() {
        //Act
        MensajeCorreo mensaje = mensajeDe(TipoNotificacion.DIETA_GENERADA, Map.of("caballo", "Trueno", "codigo", "EQ-001",
                "msTotalKg", "7.21", "forrajeKg", "6.85", "concentradoKg", "0.36"));

        //Assert
        assertTrue(mensaje.cuerpo().contains("Trueno (EQ-001)"));
        assertTrue(mensaje.cuerpo().contains("- Total: 7.21 kg"));
        assertTrue(mensaje.cuerpo().contains("- Forraje: 6.85 kg"));
        assertTrue(mensaje.cuerpo().contains("- Concentrado: 0.36 kg"));
    }

    @Test
    void estadoDeSaludIndicaElNuevoEstado() {
        //Act
        MensajeCorreo mensaje = mensajeDe(TipoNotificacion.ESTADO_SALUD_CAMBIADO,
                Map.of("caballo", "Trueno", "codigo", "EQ-001", "estadoSalud", "colicos"));

        //Assert
        assertTrue(mensaje.cuerpo().contains("cambió a: colicos"));
    }
}
