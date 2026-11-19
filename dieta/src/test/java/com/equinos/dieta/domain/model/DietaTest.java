package com.equinos.dieta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/** Prueba de Dieta: lo único editable son las observaciones. La ración es simulada. */
class DietaTest {

    @Test
    void conObservacionesSoloCambiaLasObservaciones() {
        //Arrange
        Racion racion = mock(Racion.class);
        Instant fecha = Instant.parse("2026-01-01T10:00:00Z");
        Dieta dieta = new Dieta(20L, 1L, 10L, "Trueno", "EQ-001", 5L, "adulto", "laminitis",
                new BigDecimal("500.00"), racion, "Primera", fecha);

        //Act
        Dieta resultado = dieta.conObservaciones("Segunda");

        //Assert
        assertEquals("Segunda", resultado.observaciones());
        assertEquals(20L, resultado.id());
        assertEquals("Trueno", resultado.caballoNombre());
        assertEquals(new BigDecimal("500.00"), resultado.pesoKg());
        assertSame(racion, resultado.racion());
        assertEquals(fecha, resultado.creadoEn());
    }
}
