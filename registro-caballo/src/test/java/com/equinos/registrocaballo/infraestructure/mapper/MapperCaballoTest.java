package com.equinos.registrocaballo.infraestructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository.CaballoData;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de MapperCaballo: el origen de cada conversión es simulado. */
@ExtendWith(MockitoExtension.class)
class MapperCaballoTest {

    private static final Long ID = 10L;
    private static final Long USUARIO_ID = 1L;
    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final Instant CREADO = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant ACTUALIZADO = Instant.parse("2026-01-02T10:00:00Z");

    @InjectMocks
    private MapperCaballo mapper;

    @Test
    void toCaballoCopiaLosCamposYConvierteCategoriaYEstado() {
        //Arrange
        CaballoData entidad = mock(CaballoData.class);
        lenient().when(entidad.getId()).thenReturn(ID);
        lenient().when(entidad.getUsuarioId()).thenReturn(USUARIO_ID);
        lenient().when(entidad.getNombre()).thenReturn("Trueno");
        lenient().when(entidad.getCodigo()).thenReturn("EQ-001");
        lenient().when(entidad.getPesoKg()).thenReturn(PESO);
        lenient().when(entidad.getCategoria()).thenReturn("adulto");
        lenient().when(entidad.getEstadoSalud()).thenReturn("laminitis");
        lenient().when(entidad.isActivo()).thenReturn(true);
        lenient().when(entidad.getCreadoEn()).thenReturn(CREADO);
        lenient().when(entidad.getActualizadoEn()).thenReturn(ACTUALIZADO);

        //Act
        Caballo caballo = mapper.toCaballo(entidad);

        //Assert
        assertEquals(ID, caballo.getId());
        assertEquals(USUARIO_ID, caballo.getUsuarioId());
        assertEquals("Trueno", caballo.getNombre());
        assertEquals("EQ-001", caballo.getCodigo());
        assertEquals(PESO, caballo.getPesoKg());
        assertEquals(Categoria.ADULTO, caballo.getCategoria());
        assertEquals(EstadoSalud.LAMINITIS, caballo.getEstadoSalud());
        assertTrue(caballo.isActivo());
        assertEquals(CREADO, caballo.getCreadoEn());
        assertEquals(ACTUALIZADO, caballo.getActualizadoEn());
    }

    @Test
    void toDataCopiaLosCamposYGuardaLosCodigos() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        lenient().when(caballo.getId()).thenReturn(ID);
        lenient().when(caballo.getUsuarioId()).thenReturn(USUARIO_ID);
        lenient().when(caballo.getNombre()).thenReturn("Trueno");
        lenient().when(caballo.getCodigo()).thenReturn("EQ-001");
        lenient().when(caballo.getPesoKg()).thenReturn(PESO);
        lenient().when(caballo.getCategoria()).thenReturn(Categoria.JOVEN);
        lenient().when(caballo.getEstadoSalud()).thenReturn(EstadoSalud.COLICOS);
        lenient().when(caballo.isActivo()).thenReturn(true);
        lenient().when(caballo.getCreadoEn()).thenReturn(CREADO);
        lenient().when(caballo.getActualizadoEn()).thenReturn(ACTUALIZADO);

        //Act
        CaballoData entidad = mapper.toData(caballo);

        //Assert
        assertEquals(ID, entidad.getId());
        assertEquals(USUARIO_ID, entidad.getUsuarioId());
        assertEquals("Trueno", entidad.getNombre());
        assertEquals("EQ-001", entidad.getCodigo());
        assertEquals(PESO, entidad.getPesoKg());
        assertEquals("joven", entidad.getCategoria());
        assertEquals("colicos", entidad.getEstadoSalud());
        assertTrue(entidad.isActivo());
        assertEquals(CREADO, entidad.getCreadoEn());
        assertEquals(ACTUALIZADO, entidad.getActualizadoEn());
    }
}
