package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.CategoriaCatalogo;
import com.equinos.dieta.domain.model.EstadoSaludCatalogo;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.infraestructure.mapper.MapperCatalogo;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de CatalogoDataGatewayImpl. Los repositorios JPA, el mapper y los datos son simulados. */
@ExtendWith(MockitoExtension.class)
class CatalogoDataGatewayImplTest {

    @Mock
    private MapperCatalogo mapperCatalogo;
    @Mock
    private CategoriaDataJpaRepository categorias;
    @Mock
    private EstadoSaludDataJpaRepository estados;
    @Mock
    private PerfilDietaDataJpaRepository perfiles;

    @InjectMocks
    private CatalogoDataGatewayImpl gateway;

    @Captor
    private ArgumentCaptor<List<CategoriaData>> categoriasGuardadas;
    @Captor
    private ArgumentCaptor<List<EstadoSaludData>> estadosGuardados;
    @Captor
    private ArgumentCaptor<List<PerfilDietaData>> perfilesGuardados;

    private static PerfilDieta perfilSimulado(Long id, String categoria, String estadoSalud) {
        PerfilDieta perfil = mock(PerfilDieta.class);
        lenient().when(perfil.id()).thenReturn(id);
        lenient().when(perfil.categoria()).thenReturn(categoria);
        lenient().when(perfil.estadoSalud()).thenReturn(estadoSalud);
        return perfil;
    }

    private static EstadoSaludData estadoSimulado(String codigo, String nombre) {
        EstadoSaludData estado = mock(EstadoSaludData.class);
        lenient().when(estado.getCodigo()).thenReturn(codigo);
        lenient().when(estado.getNombre()).thenReturn(nombre);
        return estado;
    }

    private static PerfilDietaData entidadSimulada(String estadoCodigo) {
        PerfilDietaData entidad = mock(PerfilDietaData.class);
        lenient().when(entidad.getEstadoCodigo()).thenReturn(estadoCodigo);
        return entidad;
    }

    /** Simula la lectura de la base: dos estados y dos perfiles ya convertidos por el mapper. */
    private void baseConDosPerfiles(PerfilDieta laminitis, PerfilDieta colicos) {
        EstadoSaludData estadoLaminitis = estadoSimulado("laminitis", "Laminitis");
        EstadoSaludData estadoColicos = estadoSimulado("colicos", "Cólicos");
        PerfilDietaData entidadLaminitis = entidadSimulada("laminitis");
        PerfilDietaData entidadColicos = entidadSimulada("colicos");
        when(estados.findAll()).thenReturn(List.of(estadoLaminitis, estadoColicos));
        when(perfiles.findAllConItems()).thenReturn(List.of(entidadLaminitis, entidadColicos));
        when(mapperCatalogo.toPerfilDieta(entidadLaminitis, "Laminitis")).thenReturn(laminitis);
        when(mapperCatalogo.toPerfilDieta(entidadColicos, "Cólicos")).thenReturn(colicos);
    }

    @Test
    void estaVacioSiNoHayPerfiles() {
        //Arrange
        when(perfiles.count()).thenReturn(0L);

        //Act y Assert
        assertTrue(gateway.estaVacio());
    }

    @Test
    void noEstaVacioSiHayPerfiles() {
        //Arrange
        when(perfiles.count()).thenReturn(15L);

        //Act y Assert
        assertFalse(gateway.estaVacio());
    }

    @Test
    void guardarInsertaCategoriasYEstadosAntesQueLosPerfiles() {
        //Arrange
        CategoriaCatalogo categoria = mock(CategoriaCatalogo.class);
        lenient().when(categoria.codigo()).thenReturn("adulto");
        lenient().when(categoria.nombre()).thenReturn("Adulto");
        lenient().when(categoria.descripcion()).thenReturn("Caballo adulto");
        lenient().when(categoria.notasNrc()).thenReturn("Notas");
        EstadoSaludCatalogo estado = mock(EstadoSaludCatalogo.class);
        lenient().when(estado.codigo()).thenReturn("laminitis");
        lenient().when(estado.nombre()).thenReturn("Laminitis");
        PerfilDieta perfil = mock(PerfilDieta.class);
        PerfilDietaData entidad = mock(PerfilDietaData.class);
        CatalogoMaestro catalogo = mock(CatalogoMaestro.class);
        lenient().when(catalogo.categorias()).thenReturn(List.of(categoria));
        lenient().when(catalogo.estados()).thenReturn(List.of(estado));
        lenient().when(catalogo.perfiles()).thenReturn(List.of(perfil));
        when(mapperCatalogo.toData(perfil)).thenReturn(entidad);

        //Act
        gateway.guardar(catalogo);

        //Assert: el flush va antes de los perfiles por las claves foráneas
        InOrder orden = inOrder(categorias, estados, perfiles);
        orden.verify(categorias).saveAll(categoriasGuardadas.capture());
        orden.verify(estados).saveAll(estadosGuardados.capture());
        orden.verify(categorias).flush();
        orden.verify(estados).flush();
        orden.verify(perfiles).saveAll(perfilesGuardados.capture());

        CategoriaData categoriaGuardada = categoriasGuardadas.getValue().get(0);
        assertEquals("adulto", categoriaGuardada.getCodigo());
        assertEquals("Adulto", categoriaGuardada.getNombre());
        assertEquals("Caballo adulto", categoriaGuardada.getDescripcion());
        assertEquals("Notas", categoriaGuardada.getNotasNrc());
        assertEquals("laminitis", estadosGuardados.getValue().get(0).getCodigo());
        assertEquals("Laminitis", estadosGuardados.getValue().get(0).getNombre());
        assertEquals(List.of(entidad), perfilesGuardados.getValue());
    }

    @Test
    void buscarPerfilEncuentraLaCombinacionDeCategoriaYEstado() {
        //Arrange
        PerfilDieta laminitis = perfilSimulado(1L, "adulto", "laminitis");
        PerfilDieta colicos = perfilSimulado(2L, "adulto", "colicos");
        baseConDosPerfiles(laminitis, colicos);

        //Act
        Optional<PerfilDieta> resultado = gateway.buscarPerfil("adulto", "colicos");

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(colicos, resultado.get());
    }

    @Test
    void buscarPerfilConOtraCategoriaDevuelveVacio() {
        //Arrange
        baseConDosPerfiles(perfilSimulado(1L, "adulto", "laminitis"), perfilSimulado(2L, "adulto", "colicos"));

        //Act
        Optional<PerfilDieta> resultado = gateway.buscarPerfil("potrillo", "laminitis");

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPerfilPorIdEncuentraElPerfil() {
        //Arrange
        PerfilDieta laminitis = perfilSimulado(1L, "adulto", "laminitis");
        PerfilDieta colicos = perfilSimulado(2L, "adulto", "colicos");
        baseConDosPerfiles(laminitis, colicos);

        //Act
        Optional<PerfilDieta> resultado = gateway.buscarPerfilPorId(2L);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(colicos, resultado.get());
    }

    @Test
    void buscarPerfilPorIdInexistenteDevuelveVacio() {
        //Arrange
        baseConDosPerfiles(perfilSimulado(1L, "adulto", "laminitis"), perfilSimulado(2L, "adulto", "colicos"));

        //Act
        Optional<PerfilDieta> resultado = gateway.buscarPerfilPorId(99L);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarPerfilesDevuelveLosPerfilesConElNombreDeSuEstado() {
        //Arrange
        PerfilDieta laminitis = perfilSimulado(1L, "adulto", "laminitis");
        PerfilDieta colicos = perfilSimulado(2L, "adulto", "colicos");
        baseConDosPerfiles(laminitis, colicos);

        //Act
        List<PerfilDieta> resultado = gateway.listarPerfiles();

        //Assert
        assertEquals(List.of(laminitis, colicos), resultado);
    }
}
