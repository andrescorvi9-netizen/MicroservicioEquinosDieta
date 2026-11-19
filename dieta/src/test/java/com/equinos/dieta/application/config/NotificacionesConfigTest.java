package com.equinos.dieta.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

/** Pruebas de NotificacionesConfig: el grupo de hilos tiene cola limitada y descarta lo que no cabe. */
class NotificacionesConfigTest {

    @Test
    void creaUnGrupoDeHilosConColaLimitada() {
        //Act
        ThreadPoolExecutor grupo = assertInstanceOf(ThreadPoolExecutor.class,
                new NotificacionesConfig().notificacionesExecutor());

        //Assert
        assertEquals(200, grupo.getQueue().remainingCapacity());
        grupo.shutdown();
    }

    @Test
    void siLaColaSeLlenaDescartaElAviso() {
        //Arrange
        ThreadPoolExecutor grupo = assertInstanceOf(ThreadPoolExecutor.class,
                new NotificacionesConfig().notificacionesExecutor());
        Runnable tarea = mock(Runnable.class);

        //Act
        grupo.getRejectedExecutionHandler().rejectedExecution(tarea, grupo);

        //Assert
        verifyNoInteractions(tarea);
        grupo.shutdown();
    }
}
