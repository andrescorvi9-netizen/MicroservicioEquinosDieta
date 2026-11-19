package com.equinos.dieta.infraestructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Porcentajes;
import com.equinos.dieta.domain.model.RequerimientoNrc;
import com.equinos.dieta.domain.model.TipoItem;
import com.equinos.dieta.infraestructure.driver_adapters.jpa_repository.PerfilDietaData;
import com.equinos.dieta.infraestructure.driver_adapters.jpa_repository.PerfilDietaItemData;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de MapperCatalogo: el origen de cada conversión es simulado. */
@ExtendWith(MockitoExtension.class)
class MapperCatalogoTest {

    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final BigDecimal ENERGIA = new BigDecimal("16.70");
    private static final BigDecimal PROTEINA = new BigDecimal("630.00");
    private static final BigDecimal CALCIO = new BigDecimal("20.00");
    private static final BigDecimal FOSFORO = new BigDecimal("14.00");
    private static final BigDecimal PCT_MS = new BigDecimal("1.5");
    private static final BigDecimal PCT_FORRAJE = new BigDecimal("95.00");
    private static final BigDecimal PCT_CONCENTRADO = new BigDecimal("5.00");

    @InjectMocks
    private MapperCatalogo mapper;

    private static PerfilDietaItemData itemSimulado(String tipo, String texto) {
        PerfilDietaItemData item = mock(PerfilDietaItemData.class);
        lenient().when(item.getTipo()).thenReturn(tipo);
        lenient().when(item.getTexto()).thenReturn(texto);
        return item;
    }

    @Test
    void toPerfilDietaCopiaLosCamposYAgrupaLosItemsPorTipo() {
        //Arrange
        PerfilDietaItemData heno = itemSimulado("forraje_base", "Heno");
        PerfilDietaItemData pasto = itemSimulado("forraje_base", "Pasto");
        PerfilDietaItemData aviso = itemSimulado("advertencia", "Consultar al veterinario");
        PerfilDietaData entidad = mock(PerfilDietaData.class);
        lenient().when(entidad.getId()).thenReturn(5L);
        lenient().when(entidad.getCategoriaCodigo()).thenReturn("adulto");
        lenient().when(entidad.getEstadoCodigo()).thenReturn("laminitis");
        lenient().when(entidad.getFuente()).thenReturn("documentado");
        lenient().when(entidad.getCriterioNutricional()).thenReturn("Bajo en almidón");
        lenient().when(entidad.getPesoRefKg()).thenReturn(PESO);
        lenient().when(entidad.getEnergiaDigestibleMcalDia()).thenReturn(ENERGIA);
        lenient().when(entidad.getProteinaCrudaGDia()).thenReturn(PROTEINA);
        lenient().when(entidad.getCalcioGDia()).thenReturn(CALCIO);
        lenient().when(entidad.getFosforoGDia()).thenReturn(FOSFORO);
        lenient().when(entidad.getPctMsPeso()).thenReturn(PCT_MS);
        lenient().when(entidad.getPctForraje()).thenReturn(PCT_FORRAJE);
        lenient().when(entidad.getPctConcentrado()).thenReturn(PCT_CONCENTRADO);
        lenient().when(entidad.getManejoAgua()).thenReturn("Agua limpia");
        lenient().when(entidad.getItems()).thenReturn(List.of(heno, pasto, aviso));

        //Act
        PerfilDieta perfil = mapper.toPerfilDieta(entidad, "Laminitis");

        //Assert
        assertEquals(5L, perfil.id());
        assertEquals("adulto", perfil.categoria());
        assertEquals("laminitis", perfil.estadoSalud());
        assertEquals("Laminitis", perfil.nombreEstado());
        assertEquals("documentado", perfil.fuente());
        assertEquals("Bajo en almidón", perfil.criterioNutricional());
        assertEquals(PESO, perfil.requerimiento().pesoReferenciaKg());
        assertEquals(ENERGIA, perfil.requerimiento().energiaDigestibleMcalDia());
        assertEquals(PROTEINA, perfil.requerimiento().proteinaCrudaGDia());
        assertEquals(CALCIO, perfil.requerimiento().calcioGDia());
        assertEquals(FOSFORO, perfil.requerimiento().fosforoGDia());
        assertEquals(PCT_MS, perfil.porcentajes().msDelPeso());
        assertEquals(PCT_FORRAJE, perfil.porcentajes().forraje());
        assertEquals(PCT_CONCENTRADO, perfil.porcentajes().concentrado());
        assertEquals("Agua limpia", perfil.manejoAgua());
        assertEquals(List.of("Heno", "Pasto"), perfil.items(TipoItem.FORRAJE_BASE));
        assertEquals(List.of("Consultar al veterinario"), perfil.items(TipoItem.ADVERTENCIA));
        assertTrue(perfil.items(TipoItem.SUPLEMENTO).isEmpty());
    }

    @Test
    void toDataCopiaLosCamposYNumeraLosItemsDeCadaTipo() {
        //Arrange
        RequerimientoNrc requerimiento = mock(RequerimientoNrc.class);
        lenient().when(requerimiento.pesoReferenciaKg()).thenReturn(PESO);
        lenient().when(requerimiento.energiaDigestibleMcalDia()).thenReturn(ENERGIA);
        lenient().when(requerimiento.proteinaCrudaGDia()).thenReturn(PROTEINA);
        lenient().when(requerimiento.calcioGDia()).thenReturn(CALCIO);
        lenient().when(requerimiento.fosforoGDia()).thenReturn(FOSFORO);
        Porcentajes porcentajes = mock(Porcentajes.class);
        lenient().when(porcentajes.msDelPeso()).thenReturn(PCT_MS);
        lenient().when(porcentajes.forraje()).thenReturn(PCT_FORRAJE);
        lenient().when(porcentajes.concentrado()).thenReturn(PCT_CONCENTRADO);
        PerfilDieta perfil = mock(PerfilDieta.class);
        lenient().when(perfil.categoria()).thenReturn("adulto");
        lenient().when(perfil.estadoSalud()).thenReturn("laminitis");
        lenient().when(perfil.fuente()).thenReturn("documentado");
        lenient().when(perfil.criterioNutricional()).thenReturn("Bajo en almidón");
        lenient().when(perfil.requerimiento()).thenReturn(requerimiento);
        lenient().when(perfil.porcentajes()).thenReturn(porcentajes);
        lenient().when(perfil.manejoAgua()).thenReturn("Agua limpia");
        lenient().when(perfil.items(any(TipoItem.class))).thenReturn(List.of());
        lenient().when(perfil.items(TipoItem.FORRAJE_BASE)).thenReturn(List.of("Heno", "Pasto"));

        //Act
        PerfilDietaData entidad = mapper.toData(perfil);

        //Assert
        assertEquals("adulto", entidad.getCategoriaCodigo());
        assertEquals("laminitis", entidad.getEstadoCodigo());
        assertEquals("documentado", entidad.getFuente());
        assertEquals("Bajo en almidón", entidad.getCriterioNutricional());
        assertEquals(PESO, entidad.getPesoRefKg());
        assertEquals(ENERGIA, entidad.getEnergiaDigestibleMcalDia());
        assertEquals(PROTEINA, entidad.getProteinaCrudaGDia());
        assertEquals(CALCIO, entidad.getCalcioGDia());
        assertEquals(FOSFORO, entidad.getFosforoGDia());
        assertEquals(PCT_MS, entidad.getPctMsPeso());
        assertEquals(PCT_FORRAJE, entidad.getPctForraje());
        assertEquals(PCT_CONCENTRADO, entidad.getPctConcentrado());
        assertEquals("Agua limpia", entidad.getManejoAgua());

        assertEquals(2, entidad.getItems().size());
        PerfilDietaItemData primero = entidad.getItems().get(0);
        PerfilDietaItemData segundo = entidad.getItems().get(1);
        assertSame(entidad, primero.getPerfil());
        assertEquals("forraje_base", primero.getTipo());
        assertEquals(1, primero.getOrden());
        assertEquals("Heno", primero.getTexto());
        assertEquals(2, segundo.getOrden());
        assertEquals("Pasto", segundo.getTexto());
    }
}
