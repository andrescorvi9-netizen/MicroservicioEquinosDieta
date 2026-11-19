package com.equinos.registrocaballo.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de Caballo. Aquí el caballo es real porque es la clase que se prueba. */
class CaballoTest {

    private static final Long USUARIO_ID = 1L;
    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final BigDecimal PESO_NUEVO = new BigDecimal("480.50");
    private static final Instant AHORA = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant DESPUES = AHORA.plusSeconds(60);

    private static Caballo caballoExistente() {
        return new Caballo(10L, USUARIO_ID, "Trueno", "EQ-001", PESO, Categoria.ADULTO,
                EstadoSalud.SIN_COMPLICACIONES, true, AHORA, AHORA);
    }

    @Test
    void nuevoLimpiaLosDatosYQuedaActivo() {
        //Act
        Caballo caballo = Caballo.nuevo(USUARIO_ID, " Trueno ", " eq-001 ", PESO, Categoria.JOVEN,
                EstadoSalud.LAMINITIS, AHORA);

        //Assert
        assertNull(caballo.getId());
        assertEquals(USUARIO_ID, caballo.getUsuarioId());
        assertEquals("Trueno", caballo.getNombre());
        assertEquals("EQ-001", caballo.getCodigo());
        assertEquals(PESO, caballo.getPesoKg());
        assertEquals(Categoria.JOVEN, caballo.getCategoria());
        assertEquals(EstadoSalud.LAMINITIS, caballo.getEstadoSalud());
        assertTrue(caballo.isActivo());
        assertEquals(AHORA, caballo.getCreadoEn());
    }

    @Test
    void normalizarCodigoConvierteNuloEnVacio() {
        //Act y Assert
        assertEquals("", Caballo.normalizarCodigo(null));
    }

    @Test
    void noSePuedeRegistrarUnPesoNuloNiCero() {
        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> Caballo.nuevo(USUARIO_ID, "Trueno", "EQ-001", null,
                Categoria.ADULTO, EstadoSalud.LAMINITIS, AHORA));
        assertThrows(ReglaNegocioException.class, () -> Caballo.nuevo(USUARIO_ID, "Trueno", "EQ-001", BigDecimal.ZERO,
                Categoria.ADULTO, EstadoSalud.LAMINITIS, AHORA));
    }

    @Test
    void actualizarCambiaTodosLosDatos() {
        //Arrange
        Caballo caballo = caballoExistente();

        //Act
        caballo.actualizar(" Rayo ", " eq-002 ", PESO_NUEVO, Categoria.POTRILLO, EstadoSalud.COLICOS, DESPUES);

        //Assert
        assertEquals("Rayo", caballo.getNombre());
        assertEquals("EQ-002", caballo.getCodigo());
        assertEquals(PESO_NUEVO, caballo.getPesoKg());
        assertEquals(Categoria.POTRILLO, caballo.getCategoria());
        assertEquals(EstadoSalud.COLICOS, caballo.getEstadoSalud());
        assertEquals(DESPUES, caballo.getActualizadoEn());
    }

    @Test
    void cambiarPesoGuardaElPesoNuevo() {
        //Arrange
        Caballo caballo = caballoExistente();

        //Act
        caballo.cambiarPeso(PESO_NUEVO, DESPUES);

        //Assert
        assertEquals(PESO_NUEVO, caballo.getPesoKg());
        assertEquals(DESPUES, caballo.getActualizadoEn());
    }

    @Test
    void unPesoNegativoSeRechazaYConservaElAnterior() {
        //Arrange
        Caballo caballo = caballoExistente();

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> caballo.cambiarPeso(new BigDecimal("-1"), DESPUES));
        assertEquals(PESO, caballo.getPesoKg());
    }

    @Test
    void cambiarEstadoSaludGuardaElEstadoNuevo() {
        //Arrange
        Caballo caballo = caballoExistente();

        //Act
        caballo.cambiarEstadoSalud(EstadoSalud.DESNUTRICION, DESPUES);

        //Assert
        assertEquals(EstadoSalud.DESNUTRICION, caballo.getEstadoSalud());
        assertEquals(DESPUES, caballo.getActualizadoEn());
    }

    @Test
    void desactivarYReactivarCambianElEstadoActivo() {
        //Arrange
        Caballo caballo = caballoExistente();

        //Act y Assert
        caballo.desactivar(DESPUES);
        assertFalse(caballo.isActivo());

        caballo.reactivar(DESPUES);
        assertTrue(caballo.isActivo());
        assertEquals(DESPUES, caballo.getActualizadoEn());
    }
}
