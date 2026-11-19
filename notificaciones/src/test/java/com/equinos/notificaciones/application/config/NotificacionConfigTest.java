package com.equinos.notificaciones.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.equinos.notificaciones.domain.model.gateway.CorreoGateway;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

/** Pruebas de NotificacionConfig: crea el caso de uso y el grupo de hilos con cola limitada. */
class NotificacionConfigTest {

    private final NotificacionConfig config = new NotificacionConfig();

    @Test
    void creaElCasoDeUso() {
        //Act y Assert
        assertNotNull(config.notificacionUseCase(mock(CorreoGateway.class)));
    }

    @Test
    void creaUnGrupoDeHilosConColaLimitada() {
        //Act
        ThreadPoolExecutor grupo = assertInstanceOf(ThreadPoolExecutor.class, config.notificacionesExecutor());

        //Assert
        assertEquals(500, grupo.getQueue().remainingCapacity());
        grupo.shutdown();
    }

    @Test
    void siLaColaSeLlenaDescartaLaNotificacion() {
        //Arrange
        ThreadPoolExecutor grupo = assertInstanceOf(ThreadPoolExecutor.class, config.notificacionesExecutor());
        Runnable tarea = mock(Runnable.class);

        //Act
        grupo.getRejectedExecutionHandler().rejectedExecution(tarea, grupo);

        //Assert
        verifyNoInteractions(tarea);
        grupo.shutdown();
    }
}
