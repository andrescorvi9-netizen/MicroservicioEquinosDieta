package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.Pagina;
import com.equinos.dieta.domain.model.Racion;

import java.math.BigDecimal;
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

/** Pruebas de DietaDataGatewayImpl. El repositorio JPA y los datos son simulados. */
@ExtendWith(MockitoExtension.class)
class DietaDataGatewayImplTest {

    private static final Long ID = 20L;
    private static final Long USUARIO_ID = 1L;
    private static final Long CABALLO_ID = 10L;
    private static final Long PERFIL_ID = 5L;
    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final BigDecimal MS_TOTAL = new BigDecimal("7.50");
    private static final BigDecimal FORRAJE = new BigDecimal("7.12");
    private static final BigDecimal CONCENTRADO = new BigDecimal("0.38");
    private static final Instant FECHA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private DietaDataJpaRepository repositorio;

    @InjectMocks
    private DietaDataGatewayImpl gateway;

    private static DietaData entidadSimulada() {
        DietaData entidad = mock(DietaData.class);
        lenient().when(entidad.getId()).thenReturn(ID);
        lenient().when(entidad.getUsuarioId()).thenReturn(USUARIO_ID);
        lenient().when(entidad.getCaballoId()).thenReturn(CABALLO_ID);
        lenient().when(entidad.getCaballoNombre()).thenReturn("Trueno");
        lenient().when(entidad.getCaballoCodigo()).thenReturn("EQ-001");
        lenient().when(entidad.getPerfilId()).thenReturn(PERFIL_ID);
        lenient().when(entidad.getCategoria()).thenReturn("adulto");
        lenient().when(entidad.getEstadoSalud()).thenReturn("laminitis");
        lenient().when(entidad.getPesoKg()).thenReturn(PESO);
        lenient().when(entidad.getMsTotalKg()).thenReturn(MS_TOTAL);
        lenient().when(entidad.getForrajeKg()).thenReturn(FORRAJE);
        lenient().when(entidad.getConcentradoKg()).thenReturn(CONCENTRADO);
        lenient().when(entidad.getObservaciones()).thenReturn("Sin novedad");
        lenient().when(entidad.getCreadoEn()).thenReturn(FECHA);
        return entidad;
    }

    private static Dieta dietaSimulada() {
        Racion racion = mock(Racion.class);
        lenient().when(racion.msTotalKg()).thenReturn(MS_TOTAL);
        lenient().when(racion.forrajeKg()).thenReturn(FORRAJE);
        lenient().when(racion.concentradoKg()).thenReturn(CONCENTRADO);

        Dieta dieta = mock(Dieta.class);
        lenient().when(dieta.id()).thenReturn(ID);
        lenient().when(dieta.usuarioId()).thenReturn(USUARIO_ID);
        lenient().when(dieta.caballoId()).thenReturn(CABALLO_ID);
        lenient().when(dieta.caballoNombre()).thenReturn("Trueno");
        lenient().when(dieta.caballoCodigo()).thenReturn("EQ-001");
        lenient().when(dieta.perfilId()).thenReturn(PERFIL_ID);
        lenient().when(dieta.categoria()).thenReturn("adulto");
        lenient().when(dieta.estadoSalud()).thenReturn("laminitis");
        lenient().when(dieta.pesoKg()).thenReturn(PESO);
        lenient().when(dieta.racion()).thenReturn(racion);
        lenient().when(dieta.observaciones()).thenReturn("Sin novedad");
        lenient().when(dieta.creadoEn()).thenReturn(FECHA);
        return dieta;
    }

    @SuppressWarnings("unchecked")
    private static Page<DietaData> paginaSimulada(DietaData entidad) {
        Page<DietaData> pagina = mock(Page.class);
        lenient().when(pagina.getContent()).thenReturn(List.of(entidad));
        lenient().when(pagina.getNumber()).thenReturn(0);
        lenient().when(pagina.getSize()).thenReturn(10);
        lenient().when(pagina.getTotalElements()).thenReturn(1L);
        lenient().when(pagina.getTotalPages()).thenReturn(1);
        return pagina;
    }

    private static void verificarDieta(Dieta dieta) {
        assertEquals(ID, dieta.id());
        assertEquals(USUARIO_ID, dieta.usuarioId());
        assertEquals(CABALLO_ID, dieta.caballoId());
        assertEquals("Trueno", dieta.caballoNombre());
        assertEquals("EQ-001", dieta.caballoCodigo());
        assertEquals(PERFIL_ID, dieta.perfilId());
        assertEquals("adulto", dieta.categoria());
        assertEquals("laminitis", dieta.estadoSalud());
        assertEquals(PESO, dieta.pesoKg());
        assertEquals(MS_TOTAL, dieta.racion().msTotalKg());
        assertEquals(FORRAJE, dieta.racion().forrajeKg());
        assertEquals(CONCENTRADO, dieta.racion().concentradoKg());
        assertEquals("Sin novedad", dieta.observaciones());
        assertEquals(FECHA, dieta.creadoEn());
    }

    private static void verificarOrdenRecientesPrimero(Pageable orden) {
        Sort.Order porFecha = orden.getSort().getOrderFor("creadoEn");
        Sort.Order porId = orden.getSort().getOrderFor("id");
        assertNotNull(porFecha);
        assertNotNull(porId);
        assertTrue(porFecha.isDescending());
        assertTrue(porId.isDescending());
    }

    @Test
    void guardarConvierteTodosLosCamposYDevuelveLaDieta() {
        //Arrange
        DietaData guardada = entidadSimulada();
        when(repositorio.save(any(DietaData.class))).thenReturn(guardada);

        //Act
        Dieta resultado = gateway.guardar(dietaSimulada());

        //Assert
        verificarDieta(resultado);
        ArgumentCaptor<DietaData> enviada = ArgumentCaptor.forClass(DietaData.class);
        verify(repositorio).save(enviada.capture());
        assertEquals(ID, enviada.getValue().getId());
        assertEquals(USUARIO_ID, enviada.getValue().getUsuarioId());
        assertEquals(CABALLO_ID, enviada.getValue().getCaballoId());
        assertEquals("Trueno", enviada.getValue().getCaballoNombre());
        assertEquals("EQ-001", enviada.getValue().getCaballoCodigo());
        assertEquals(PERFIL_ID, enviada.getValue().getPerfilId());
        assertEquals("adulto", enviada.getValue().getCategoria());
        assertEquals("laminitis", enviada.getValue().getEstadoSalud());
        assertEquals(PESO, enviada.getValue().getPesoKg());
        assertEquals(MS_TOTAL, enviada.getValue().getMsTotalKg());
        assertEquals(FORRAJE, enviada.getValue().getForrajeKg());
        assertEquals(CONCENTRADO, enviada.getValue().getConcentradoKg());
        assertEquals("Sin novedad", enviada.getValue().getObservaciones());
        assertEquals(FECHA, enviada.getValue().getCreadoEn());
    }

    @Test
    void buscarDevuelveLaDietaEncontrada() {
        //Arrange
        DietaData entidad = entidadSimulada();
        when(repositorio.findByIdAndUsuarioId(ID, USUARIO_ID)).thenReturn(Optional.of(entidad));

        //Act
        Optional<Dieta> resultado = gateway.buscar(ID, USUARIO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        verificarDieta(resultado.get());
    }

    @Test
    void buscarSinResultadoDevuelveVacio() {
        //Arrange
        when(repositorio.findByIdAndUsuarioId(ID, USUARIO_ID)).thenReturn(Optional.empty());

        //Act
        Optional<Dieta> resultado = gateway.buscar(ID, USUARIO_ID);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void existeRespondeLoQueDiceElRepositorio() {
        //Arrange
        when(repositorio.existsByIdAndUsuarioId(ID, USUARIO_ID)).thenReturn(true);
        when(repositorio.existsByIdAndUsuarioId(21L, USUARIO_ID)).thenReturn(false);

        //Act y Assert
        assertTrue(gateway.existe(ID, USUARIO_ID));
        assertFalse(gateway.existe(21L, USUARIO_ID));
    }

    @Test
    void listarPideLasDietasMasRecientesPrimero() {
        //Arrange
        Page<DietaData> pagina = paginaSimulada(entidadSimulada());
        when(repositorio.findByUsuarioId(eq(USUARIO_ID), any(Pageable.class))).thenReturn(pagina);

        //Act
        Pagina<Dieta> resultado = gateway.listar(USUARIO_ID, 2, 5);

        //Assert
        assertEquals(1, resultado.contenido().size());
        verificarDieta(resultado.contenido().get(0));
        assertEquals(0, resultado.pagina());
        assertEquals(10, resultado.tamano());
        assertEquals(1L, resultado.totalElementos());
        assertEquals(1, resultado.totalPaginas());

        ArgumentCaptor<Pageable> orden = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findByUsuarioId(eq(USUARIO_ID), orden.capture());
        assertEquals(2, orden.getValue().getPageNumber());
        assertEquals(5, orden.getValue().getPageSize());
        verificarOrdenRecientesPrimero(orden.getValue());
    }

    @Test
    void listarPorCaballoPideElHistorialMasRecientePrimero() {
        //Arrange
        Page<DietaData> pagina = paginaSimulada(entidadSimulada());
        when(repositorio.findByUsuarioIdAndCaballoId(eq(USUARIO_ID), eq(CABALLO_ID), any(Pageable.class)))
                .thenReturn(pagina);

        //Act
        Pagina<Dieta> resultado = gateway.listarPorCaballo(USUARIO_ID, CABALLO_ID, 0, 10);

        //Assert
        verificarDieta(resultado.contenido().get(0));
        ArgumentCaptor<Pageable> orden = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findByUsuarioIdAndCaballoId(eq(USUARIO_ID), eq(CABALLO_ID), orden.capture());
        verificarOrdenRecientesPrimero(orden.getValue());
    }

    @Test
    void buscarVigenteDevuelveLaDietaMasReciente() {
        //Arrange
        DietaData entidad = entidadSimulada();
        when(repositorio.findFirstByUsuarioIdAndCaballoIdOrderByCreadoEnDescIdDesc(USUARIO_ID, CABALLO_ID))
                .thenReturn(Optional.of(entidad));

        //Act
        Optional<Dieta> resultado = gateway.buscarVigente(USUARIO_ID, CABALLO_ID);

        //Assert
        assertTrue(resultado.isPresent());
        verificarDieta(resultado.get());
    }

    @Test
    void buscarVigenteSinDietasDevuelveVacio() {
        //Arrange
        when(repositorio.findFirstByUsuarioIdAndCaballoIdOrderByCreadoEnDescIdDesc(USUARIO_ID, CABALLO_ID))
                .thenReturn(Optional.empty());

        //Act
        Optional<Dieta> resultado = gateway.buscarVigente(USUARIO_ID, CABALLO_ID);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void eliminarBorraEnElRepositorio() {
        //Act
        gateway.eliminar(ID);

        //Assert
        verify(repositorio).deleteById(ID);
    }

    @Test
    void eliminarPorUsuarioDevuelveCuantasBorro() {
        //Arrange
        when(repositorio.deleteByUsuarioId(USUARIO_ID)).thenReturn(4L);

        //Act
        long resultado = gateway.eliminarPorUsuario(USUARIO_ID);

        //Assert
        assertEquals(4L, resultado);
    }
}
