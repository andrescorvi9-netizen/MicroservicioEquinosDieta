package com.equinos.notificaciones.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.equinos.notificaciones.domain.model.Notificacion;
import com.equinos.notificaciones.domain.model.TipoNotificacion;
import com.equinos.notificaciones.domain.usecase.NotificacionUseCase;

import java.util.Map;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Pruebas de InternalNotificacionController. El caso de uso y los hilos son simulados. */
@ExtendWith(MockitoExtension.class)
class InternalNotificacionControllerTest {

    @Mock
    private NotificacionUseCase useCase;

    private static NotificacionRequest requestSimulado(Notificacion notificacion) {
        NotificacionRequest request = mock(NotificacionRequest.class);
        lenient().when(request.aDominio()).thenReturn(notificacion);
        return request;
    }

    @Test
    void recibeLaNotificacionYRespondeAceptada() {
        //Arrange
        Notificacion notificacion = mock(Notificacion.class);
        InternalNotificacionController controller = new InternalNotificacionController(useCase, Runnable::run);

        //Act
        ResponseEntity<Void> respuesta = controller.recibir(requestSimulado(notificacion));

        //Assert
        assertEquals(HttpStatus.ACCEPTED, respuesta.getStatusCode());
        verify(useCase).enviar(notificacion);
    }

    @Test
    void noEsperaAQueElCorreoSeEnvie() {
        //Arrange
        Executor hilos = mock(Executor.class);
        InternalNotificacionController controller = new InternalNotificacionController(useCase, hilos);

        //Act
        ResponseEntity<Void> respuesta = controller.recibir(requestSimulado(mock(Notificacion.class)));

        //Assert
        assertEquals(HttpStatus.ACCEPTED, respuesta.getStatusCode());
        verifyNoInteractions(useCase);
    }

    @Test
    void elRequestSeConvierteAlDominio() {
        //Arrange
        NotificacionRequest request = new NotificacionRequest("ana@correo.com", "Ana", TipoNotificacion.BIENVENIDA,
                Map.of("clave", "valor"));

        //Act
        Notificacion notificacion = request.aDominio();

        //Assert
        assertEquals("ana@correo.com", notificacion.correo());
        assertEquals("Ana", notificacion.nombre());
        assertEquals(TipoNotificacion.BIENVENIDA, notificacion.tipo());
        assertEquals("valor", notificacion.dato("clave"));
    }
}
