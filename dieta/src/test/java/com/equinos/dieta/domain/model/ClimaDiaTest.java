package com.equinos.dieta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Pruebas de ClimaDia: el índice de estrés térmico y la recomendación de cada nivel. */
class ClimaDiaTest {

    private static final String ZONA = "Zona";

    @Test
    void elIndiceSumaGradosFahrenheitYHumedad() {
        //Arrange: 30 °C son 86 °F
        ClimaDia clima = new ClimaDia(ZONA, 30, 70);

        //Act y Assert
        assertEquals(156, clima.indice());
    }

    @Test
    void eligeLaHoraMasCalurosaConSuHumedad() {
        //Act: la madrugada fría y húmeda (18 °C con 99 %) no debe contar como calor
        ClimaDia peor = ClimaDia.deLaHoraMasCalurosa(ZONA, List.of(18.0, 30.0, 26.0), List.of(99.0, 60.0, 90.0)).orElseThrow();

        //Assert
        assertEquals(30.0, peor.temperaturaC());
        assertEquals(60, peor.humedad());
    }

    @Test
    void ignoraLasHorasSinDatos() {
        //Act
        ClimaDia peor = ClimaDia.deLaHoraMasCalurosa(ZONA, Arrays.asList(null, 20.0, 40.0), Arrays.asList(50.0, 50.0, null))
                .orElseThrow();

        //Assert
        assertEquals(20.0, peor.temperaturaC());
    }

    @Test
    void sinDatosNoHayClima() {
        //Act y Assert
        assertTrue(ClimaDia.deLaHoraMasCalurosa(ZONA, List.of(), List.of()).isEmpty());
    }

    @Test
    void cadaNivelTieneSuRecomendacion() {
        //Act y Assert
        assertTrue(new ClimaDia(ZONA, 15, 40).aviso().endsWith("Sin estrés térmico relevante."));
        assertTrue(new ClimaDia(ZONA, 20, 65).aviso().contains("Precaución"));
        assertTrue(new ClimaDia(ZONA, 28, 70).aviso().contains("Riesgo"));
        assertTrue(new ClimaDia(ZONA, 35, 85).aviso().contains("Peligro"));
    }

    @Test
    void elAvisoAclaraQueEsUnaZonaDeReferencia() {
        //Act
        String aviso = new ClimaDia("Medellín", 25.5, 80).aviso();

        //Assert
        assertTrue(aviso.startsWith("Clima de referencia de Medellín (no es la ubicación del caballo): hasta 25.5 °C"));
    }
}
