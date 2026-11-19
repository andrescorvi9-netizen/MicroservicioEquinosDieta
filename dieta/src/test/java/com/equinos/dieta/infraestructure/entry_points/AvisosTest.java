package com.equinos.dieta.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.equinos.dieta.domain.model.PerfilDieta;

import org.junit.jupiter.api.Test;

/** Pruebas de Avisos: el aviso de clima se agrega solo cuando existe. */
class AvisosTest {

    @Test
    void sinClimaDevuelveSoloElAvisoGeneral() {
        //Act y Assert
        assertEquals(Avisos.GENERAL, Avisos.para(mock(PerfilDieta.class), null));
    }

    @Test
    void conClimaLoAgregaAlFinal() {
        //Act y Assert
        assertEquals(Avisos.GENERAL + " Clima", Avisos.para(mock(PerfilDieta.class), "Clima"));
    }
}
