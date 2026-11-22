package com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;
import com.equinos.registrocaballo.infraestructure.mapper.MapperCaballo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Pruebas de CaballoDataGatewayImpl. El repositorio JPA, el mapper y los datos son simulados. */
@ExtendWith(MockitoExtension.class)
class CaballoDataGatewayImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long ID = 10L;
    private static final String CODIGO = "EQ-001";

    @Mock
    private MapperCaballo mapperCaballo;
    @Mock
    private CaballoDataJpaRepository repositorio;

    @InjectMocks
    private CaballoDataGatewayImpl gateway;

    /** Página simulada de Spring Data con una sola entidad. */
    @SuppressWarnings("unchecked")
    private static Page<CaballoData> paginaSimulada(CaballoData entidad) {
        Page<CaballoData> pagina = mock(Page.class);
        lenient().when(pagina.getContent()).thenReturn(List.of(entidad));
        lenient().when(pagina.getNumber()).thenReturn(0);
        lenient().when(pagina.getSize()).thenReturn(10);
        lenient().when(pagina.getTotalElements()).thenReturn(1L);
        lenient().when(pagina.getTotalPages()).thenReturn(1);
        return pagina;
    }

    private static void verificarPagina(Pagina<Caballo> resultado, Caballo caballo) {
        assertEquals(List.of(caballo), resultado.contenido());
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamano());
        assertEquals(1L, resultado.totalElementos());
        assertEquals(1, resultado.totalPaginas());
    }

    @Test
    void guardarConvierteGuardaYDevuelveElCaballo() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        Caballo caballoGuardado = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        CaballoData entidadGuardada = mock(CaballoData.class);
        when(mapperCaballo.toData(caballo)).thenReturn(entidad);
        when(repositorio.save(entidad)).thenReturn(entidadGuardada);
        when(mapperCaballo.toCaballo(entidadGuardada)).thenReturn(caballoGuardado);

        //Act
        Caballo resultado = gateway.guardar(caballo);

        //Assert
        assertSame(caballoGuardado, resultado);
    }

    @Test
    void buscarActivoDevuelveElCaballoEncontrado() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        when(repositorio.findByIdAndUsuarioIdAndActivoTrue(ID, USUARIO_ID)).thenReturn(Optional.of(entidad));
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Optional<Caballo> resultado = gateway.buscarActivo(ID, USUARIO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(caballo, resultado.get());
    }

    @Test
    void buscarActivoSinResultadoDevuelveVacioYNoConvierte() {
        //Arrange
        when(repositorio.findByIdAndUsuarioIdAndActivoTrue(ID, USUARIO_ID)).thenReturn(Optional.empty());

        //Act
        Optional<Caballo> resultado = gateway.buscarActivo(ID, USUARIO_ID);

        //Assert
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(mapperCaballo);
    }

    @Test
    void buscarCualquieraIncluyeLosCaballosBorrados() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        when(repositorio.findByIdAndUsuarioId(ID, USUARIO_ID)).thenReturn(Optional.of(entidad));
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Optional<Caballo> resultado = gateway.buscarCualquiera(ID, USUARIO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(caballo, resultado.get());
    }

    @Test
    void buscarActivoPorCodigoDevuelveElCaballoEncontrado() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        when(repositorio.findByCodigoAndUsuarioIdAndActivoTrue(CODIGO, USUARIO_ID)).thenReturn(Optional.of(entidad));
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Optional<Caballo> resultado = gateway.buscarActivoPorCodigo(CODIGO, USUARIO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(caballo, resultado.get());
    }

    @Test
    void existeCodigoActivoRespondeLoQueDiceElRepositorio() {
        //Arrange
        when(repositorio.existsByCodigoAndActivoTrue(CODIGO)).thenReturn(true);
        when(repositorio.existsByCodigoAndActivoTrue("EQ-002")).thenReturn(false);

        //Act y Assert
        assertTrue(gateway.existeCodigoActivo(CODIGO));
        assertFalse(gateway.existeCodigoActivo("EQ-002"));
    }

    @Test
    void listarActivosPideLaPaginaOrdenadaPorNombre() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        Page<CaballoData> pagina = paginaSimulada(entidad);
        when(repositorio.findByUsuarioIdAndActivoTrue(eq(USUARIO_ID), any(Pageable.class))).thenReturn(pagina);
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Pagina<Caballo> resultado = gateway.listarActivos(USUARIO_ID, 2, 5);

        //Assert
        verificarPagina(resultado, caballo);
        ArgumentCaptor<Pageable> orden = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findByUsuarioIdAndActivoTrue(eq(USUARIO_ID), orden.capture());
        assertEquals(2, orden.getValue().getPageNumber());
        assertEquals(5, orden.getValue().getPageSize());
        Sort.Order porNombre = orden.getValue().getSort().getOrderFor("nombre");
        assertNotNull(porNombre);
        assertTrue(porNombre.isAscending());
    }

    @Test
    void buscarActivosConCategoriaYEstadoAplicaLosDosFiltros() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        Page<CaballoData> pagina = paginaSimulada(entidad);
        when(repositorio.findByUsuarioIdAndActivoTrueAndCategoriaAndEstadoSalud(eq(USUARIO_ID), eq("adulto"),
                eq("laminitis"), any(Pageable.class))).thenReturn(pagina);
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Pagina<Caballo> resultado = gateway.buscarActivos(USUARIO_ID, Categoria.ADULTO, EstadoSalud.LAMINITIS, 0, 10);

        //Assert
        verificarPagina(resultado, caballo);
    }

    @Test
    void buscarActivosSoloConCategoriaAplicaEseFiltro() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        Page<CaballoData> pagina = paginaSimulada(entidad);
        when(repositorio.findByUsuarioIdAndActivoTrueAndCategoria(eq(USUARIO_ID), eq("joven"), any(Pageable.class)))
                .thenReturn(pagina);
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Pagina<Caballo> resultado = gateway.buscarActivos(USUARIO_ID, Categoria.JOVEN, null, 0, 10);

        //Assert
        verificarPagina(resultado, caballo);
    }

    @Test
    void buscarActivosSoloConEstadoAplicaEseFiltro() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        Page<CaballoData> pagina = paginaSimulada(entidad);
        when(repositorio.findByUsuarioIdAndActivoTrueAndEstadoSalud(eq(USUARIO_ID), eq("colicos"), any(Pageable.class)))
                .thenReturn(pagina);
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Pagina<Caballo> resultado = gateway.buscarActivos(USUARIO_ID, null, EstadoSalud.COLICOS, 0, 10);

        //Assert
        verificarPagina(resultado, caballo);
    }

    @Test
    void buscarActivosSinFiltrosListaTodos() {
        //Arrange
        Caballo caballo = mock(Caballo.class);
        CaballoData entidad = mock(CaballoData.class);
        Page<CaballoData> pagina = paginaSimulada(entidad);
        when(repositorio.findByUsuarioIdAndActivoTrue(eq(USUARIO_ID), any(Pageable.class))).thenReturn(pagina);
        when(mapperCaballo.toCaballo(entidad)).thenReturn(caballo);

        //Act
        Pagina<Caballo> resultado = gateway.buscarActivos(USUARIO_ID, null, null, 0, 10);

        //Assert
        verificarPagina(resultado, caballo);
    }

    @Test
    void eliminarTodosDeUsuarioDevuelveCuantosBorro() {
        //Arrange
        when(repositorio.eliminarTodosDeUsuario(USUARIO_ID)).thenReturn(4);

        //Act
        int resultado = gateway.eliminarTodosDeUsuario(USUARIO_ID);

        //Assert
        assertEquals(4, resultado);
    }
}
