package com.equinos.dieta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.equinos.dieta.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas del cálculo de la ración. Los porcentajes del perfil son simulados. */
@ExtendWith(MockitoExtension.class)
class RacionTest {

    private static Porcentajes porcentajesSimulados() {
        Porcentajes porcentajes = mock(Porcentajes.class);
        lenient().when(porcentajes.msDelPeso()).thenReturn(new BigDecimal("1.5"));
        lenient().when(porcentajes.forraje()).thenReturn(new BigDecimal("95"));
        return porcentajes;
    }

    @Test
    void calcularRepartelaMateriaSecaEntreForrajeYConcentrado() {
        //Act
        Racion racion = Racion.calcular(new BigDecimal("500"), porcentajesSimulados());

        //Assert
        assertEquals(new BigDecimal("7.50"), racion.msTotalKg());
        assertEquals(new BigDecimal("7.12"), racion.forrajeKg());
        assertEquals(new BigDecimal("0.38"), racion.concentradoKg());
    }

    @Test
    void unPesoNuloCeroONegativoSeRechaza() {
        //Arrange
        Porcentajes porcentajes = porcentajesSimulados();

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> Racion.calcular(null, porcentajes));
        assertThrows(ReglaNegocioException.class, () -> Racion.calcular(BigDecimal.ZERO, porcentajes));
        assertThrows(ReglaNegocioException.class, () -> Racion.calcular(new BigDecimal("-1"), porcentajes));
    }
}
