package com.equinos.notificaciones;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

/** Prueba del arranque: SpringApplication es simulado, así que el servicio no se levanta de verdad. */
class NotificacionesApplicationTest {

    @Test
    void mainArrancaLaAplicacion() {
        //Arrange
        String[] argumentos = {};

        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            //Act
            NotificacionesApplication.main(argumentos);

            //Assert
            spring.verify(() -> SpringApplication.run(NotificacionesApplication.class, argumentos));
        }
    }
}
