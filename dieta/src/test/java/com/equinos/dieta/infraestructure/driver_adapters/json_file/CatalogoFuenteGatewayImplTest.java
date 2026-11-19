package com.equinos.dieta.infraestructure.driver_adapters.json_file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.TipoItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.core.io.ClassPathResource;

/** Pruebas de CatalogoFuenteGatewayImpl. El archivo se simula: ClassPathResource entrega un JSON de prueba. */
class CatalogoFuenteGatewayImplTest {

    private static final String RECURSO = "catalogo/simulado.json";

    private static final String REQUERIMIENTO = """
            "requerimiento_nrc_referencia": {"peso_actual_kg_ejemplo": 500, "ED_Mcal_dia": 16.7,
                                             "PC_g_dia": 630, "Ca_g_dia": 20, "P_g_dia": 14}
            """;

    private static final String CATALOGO_VALIDO = """
            {"categorias": {"adulto": {"nombre": "Adulto", "descripcion": "Caballo adulto",
              "dietas_por_estado_salud": {"laminitis": {
                "nombre_estado": "Laminitis", "fuente": "inferido", "criterio_nutricional": "Bajo en almidon",
                "manejo_agua": "Agua limpia",
                %s,
                "plan_racion_estimado": {"materia_seca_total_kg_dia": 7.5, "forraje_kg_ms_dia": 7.125},
                "forraje_base": ["Heno", "Pasto"],
                "suplementos": "no es una lista"
              }}}}}
            """.formatted(REQUERIMIENTO);

    private final CatalogoFuenteGatewayImpl gateway = new CatalogoFuenteGatewayImpl(RECURSO);

    /** Ejecuta la lectura con un ClassPathResource simulado que entrega el contenido indicado. */
    private CatalogoMaestro leerContenido(String json) {
        try (MockedConstruction<ClassPathResource> archivo = mockConstruction(ClassPathResource.class,
                (recurso, contexto) -> when(recurso.getInputStream())
                        .thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))) {
            return gateway.leer();
        }
    }

    @Test
    void leerConvierteCategoriasEstadosYPerfiles() {
        //Act
        CatalogoMaestro catalogo = leerContenido(CATALOGO_VALIDO);

        //Assert
        assertEquals(1, catalogo.categorias().size());
        assertEquals("adulto", catalogo.categorias().get(0).codigo());
        assertEquals("Adulto", catalogo.categorias().get(0).nombre());
        assertEquals("Caballo adulto", catalogo.categorias().get(0).descripcion());
        assertEquals(1, catalogo.estados().size());
        assertEquals("laminitis", catalogo.estados().get(0).codigo());
        assertEquals("Laminitis", catalogo.estados().get(0).nombre());

        PerfilDieta perfil = catalogo.perfiles().get(0);
        assertEquals("adulto", perfil.categoria());
        assertEquals("laminitis", perfil.estadoSalud());
        assertTrue(perfil.esInferido());
        assertEquals("Agua limpia", perfil.manejoAgua());
        assertEquals(new BigDecimal("500"), perfil.requerimiento().pesoReferenciaKg());
        assertEquals(new BigDecimal("16.7"), perfil.requerimiento().energiaDigestibleMcalDia());
        assertEquals(new BigDecimal("1.5"), perfil.porcentajes().msDelPeso());
        assertEquals(new BigDecimal("95"), perfil.porcentajes().forraje());
        assertEquals(new BigDecimal("5"), perfil.porcentajes().concentrado());
        assertEquals(List.of("Heno", "Pasto"), perfil.items(TipoItem.FORRAJE_BASE));
    }

    @Test
    void unCampoDeTextoAusenteQuedaVacio() {
        //Act
        CatalogoMaestro catalogo = leerContenido(CATALOGO_VALIDO);

        //Assert: el JSON de prueba no trae notas_nrc
        assertEquals("", catalogo.categorias().get(0).notasNrc());
    }

    @Test
    void unaListaAusenteOConOtroTipoQuedaVacia() {
        //Act
        PerfilDieta perfil = leerContenido(CATALOGO_VALIDO).perfiles().get(0);

        //Assert
        assertTrue(perfil.items(TipoItem.SUPLEMENTO).isEmpty());
        assertTrue(perfil.items(TipoItem.ADVERTENCIA).isEmpty());
    }

    @Test
    void siElArchivoNoSePuedeLeerSeIndicaCual() {
        //Act
        IllegalStateException error;
        try (MockedConstruction<ClassPathResource> archivo = mockConstruction(ClassPathResource.class,
                (recurso, contexto) -> when(recurso.getInputStream()).thenThrow(new IOException("no existe")))) {
            error = assertThrows(IllegalStateException.class, gateway::leer);
        }

        //Assert
        assertEquals("No se pudo leer el archivo maestro de dietas: " + RECURSO, error.getMessage());
    }

    @Test
    void unJsonQueNoEsUnObjetoSeRechaza() {
        //Act
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> leerContenido("[1, 2]"));

        //Assert
        assertTrue(error.getMessage().contains("No se pudo leer el archivo maestro"));
    }

    @Test
    void unaEstructuraInesperadaSeRechaza() {
        //Act
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> leerContenido("{\"categorias\": []}"));

        //Assert
        assertEquals("El archivo maestro no tiene la estructura esperada", error.getMessage());
    }

    @Test
    void unCatalogoSinPerfilesSeRechaza() {
        //Arrange
        String sinPerfiles = "{\"categorias\": {\"adulto\": {\"nombre\": \"Adulto\", "
                + "\"dietas_por_estado_salud\": {}}}}";

        //Act
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> leerContenido(sinPerfiles));

        //Assert
        assertEquals("El archivo maestro no contiene perfiles de dieta: " + RECURSO, error.getMessage());
    }

    @Test
    void unNumeroAusenteSeRechazaIndicandoElCampo() {
        //Arrange
        String sinPeso = CATALOGO_VALIDO.replace("\"peso_actual_kg_ejemplo\": 500, ", "");

        //Act
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> leerContenido(sinPeso));

        //Assert
        assertEquals("El archivo maestro no trae el número 'peso_actual_kg_ejemplo'", error.getMessage());
    }
}
