package com.equinos.dieta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de PerfilDieta. El requerimiento y los porcentajes son simulados. */
@ExtendWith(MockitoExtension.class)
class PerfilDietaTest {

    private static PerfilDieta perfil(String fuente) {
        Porcentajes porcentajes = mock(Porcentajes.class);
        lenient().when(porcentajes.msDelPeso()).thenReturn(new BigDecimal("1.5"));
        lenient().when(porcentajes.forraje()).thenReturn(new BigDecimal("95.00"));
        lenient().when(porcentajes.concentrado()).thenReturn(new BigDecimal("5.00"));
        return new PerfilDieta(5L, "adulto", "laminitis", "Laminitis", fuente, "Bajo en almidón",
                mock(RequerimientoNrc.class), porcentajes, "Agua limpia",
                Map.of(TipoItem.FORRAJE_BASE, List.of("Heno")));
    }

    @Test
    void esInferidoSoloSiLaFuenteLoIndica() {
        //Act y Assert
        assertTrue(perfil(PerfilDieta.FUENTE_INFERIDO).esInferido());
        assertFalse(perfil("documentado").esInferido());
    }

    @Test
    void itemsDevuelveLaListaDelTipoOVacia() {
        //Arrange
        PerfilDieta perfil = perfil("documentado");

        //Act y Assert
        assertEquals(List.of("Heno"), perfil.items(TipoItem.FORRAJE_BASE));
        assertTrue(perfil.items(TipoItem.SUPLEMENTO).isEmpty());
    }

    @Test
    void criterioRacionDescribeLosPorcentajesSinCerosDeMas() {
        //Act
        String criterio = perfil("documentado").criterioRacion();

        //Assert
        assertEquals("1.5% del peso corporal en MS/día (95% forraje / 5% concentrado)", criterio);
    }
}
