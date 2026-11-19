package com.equinos.dieta.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.gateway.CatalogoFuenteGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas de CatalogoUseCase. Todo es simulado con Mockito: el catálogo, el archivo maestro
 * y los perfiles que contienen.
 */
@ExtendWith(MockitoExtension.class)
class CatalogoUseCaseTest {

    @Mock
    private CatalogoGateway catalogo;
    @Mock
    private CatalogoFuenteGateway fuente;

    @InjectMocks
    private CatalogoUseCase useCase;

    /** Perfil simulado: solo importan su categoría y su estado de salud para filtrar. */
    private static PerfilDieta perfilSimulado(String categoria, String estadoSalud) {
        PerfilDieta perfil = mock(PerfilDieta.class);
        lenient().when(perfil.categoria()).thenReturn(categoria);
        lenient().when(perfil.estadoSalud()).thenReturn(estadoSalud);
        return perfil;
    }

    private final PerfilDieta adultoLaminitis = perfilSimulado("adulto", "laminitis");
    private final PerfilDieta adultoSano = perfilSimulado("adulto", "sin_complicaciones");
    private final PerfilDieta jovenLaminitis = perfilSimulado("joven", "laminitis");

    private void catalogoConTresPerfiles() {
        when(catalogo.listarPerfiles()).thenReturn(List.of(adultoLaminitis, adultoSano, jovenLaminitis));
    }

    @Test
    void sinFiltrosDevuelveTodoElCatalogo() {
        //Arrange
        catalogoConTresPerfiles();

        //Act
        List<PerfilDieta> resultado = useCase.perfiles(null, "  ");

        //Assert
        assertEquals(3, resultado.size());
    }

    @Test
    void filtraPorCategoriaIgnorandoMayusculasYEspacios() {
        //Arrange
        catalogoConTresPerfiles();

        //Act
        List<PerfilDieta> resultado = useCase.perfiles(" ADULTO ", null);

        //Assert
        assertEquals(List.of(adultoLaminitis, adultoSano), resultado);
    }

    @Test
    void filtraPorEstadoDeSalud() {
        //Arrange
        catalogoConTresPerfiles();

        //Act
        List<PerfilDieta> resultado = useCase.perfiles(null, "Laminitis");

        //Assert
        assertEquals(List.of(adultoLaminitis, jovenLaminitis), resultado);
    }

    @Test
    void filtraPorCategoriaYEstadoALaVez() {
        //Arrange
        catalogoConTresPerfiles();

        //Act y Assert
        assertEquals(List.of(jovenLaminitis), useCase.perfiles("joven", "laminitis"));
        assertTrue(useCase.perfiles("potrillo", "laminitis").isEmpty());
    }

    @Test
    void conElCatalogoVacioLoCargaDesdeElArchivoMaestro() {
        //Arrange
        CatalogoMaestro maestro = mock(CatalogoMaestro.class);
        lenient().when(maestro.perfiles()).thenReturn(List.of(adultoLaminitis, adultoSano));
        when(catalogo.estaVacio()).thenReturn(true);
        when(fuente.leer()).thenReturn(maestro);

        //Act
        int cargados = useCase.cargarSiVacio();

        //Assert
        assertEquals(2, cargados);
        verify(catalogo).guardar(maestro);
    }

    @Test
    void siElCatalogoYaTieneDatosNoLeeElArchivoNiGuarda() {
        //Arrange
        when(catalogo.estaVacio()).thenReturn(false);

        //Act
        int cargados = useCase.cargarSiVacio();

        //Assert
        assertEquals(0, cargados);
        verifyNoInteractions(fuente);
        verify(catalogo, never()).guardar(any());
    }

    @Test
    void normalizarQuitaEspaciosPasaAMinusculasYConvierteNuloEnVacio() {
        //Act y Assert
        assertEquals("adulto", CatalogoUseCase.normalizar("  ADULTO "));
        assertEquals("", CatalogoUseCase.normalizar(null));
    }
}
