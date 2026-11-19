package com.equinos.notificaciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/** Pruebas de Notificacion: los datos son opcionales. */
class NotificacionTest {

    @Test
    void sinDatosQuedaUnMapaVacio() {
        //Act
        Notificacion notificacion = new Notificacion("ana@correo.com", "Ana", TipoNotificacion.BIENVENIDA, null);

        //Assert
        assertTrue(notificacion.datos().isEmpty());
    }

    @Test
    void unDatoAusenteSeMuestraConUnGuion() {
        //Arrange
        Notificacion notificacion = new Notificacion("ana@correo.com", "Ana", TipoNotificacion.DIETA_GENERADA,
                Map.of("caballo", "Trueno"));

        //Act y Assert
        assertEquals("Trueno", notificacion.dato("caballo"));
        assertEquals("-", notificacion.dato("codigo"));
    }
}
