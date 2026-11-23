package com.equinos.notificaciones.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/** Pruebas de HoraColombia */
class HoraColombiaTest {

    @Test
    void convierteUnaFechaUtcALaHoraDeColombia() {
        //Arrange
        Instant fecha = Instant.parse("2026-11-22T22:30:00Z");

        //Act
        OffsetDateTime resultado = HoraColombia.de(fecha);

        //Assert
        assertEquals(OffsetDateTime.parse("2026-11-22T17:30:00-05:00"), resultado);
        assertEquals(ZoneOffset.ofHours(-5), resultado.getOffset());
        assertEquals(fecha, resultado.toInstant());
    }

    @Test
    void cambiaDeDiaCuandoEnUtcEsAntesDeLas5DeLaManana() {
        //Arrange
        Instant fecha = Instant.parse("2026-01-01T03:00:00Z");

        //Act
        OffsetDateTime resultado = HoraColombia.de(fecha);

        //Assert
        assertEquals(OffsetDateTime.parse("2025-12-31T22:00:00-05:00"), resultado);
    }

    @Test
    void unaFechaVaciaSigueVacia() {
        //Arrange
        Instant fecha = null;

        //Act
        OffsetDateTime resultado = HoraColombia.de(fecha);

        //Assert
        assertNull(resultado);
    }

    @Test
    void sePuedeCrearUnaInstancia() {
        //Act
        HoraColombia hora = new HoraColombia();

        //Assert
        assertNotNull(hora);
    }
}
