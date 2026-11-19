package com.equinos.dieta.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.exception.RecursoNoEncontradoException;
import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.DietaDetallada;
import com.equinos.dieta.domain.model.Pagina;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Porcentajes;
import com.equinos.dieta.domain.model.Racion;
import com.equinos.dieta.domain.model.RequerimientoNrc;
import com.equinos.dieta.domain.model.TipoItem;
import com.equinos.dieta.domain.model.VistaPrevia;
import com.equinos.dieta.domain.usecase.CatalogoUseCase;
import com.equinos.dieta.domain.usecase.DietaUseCase;
import com.equinos.dieta.infraestructure.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Pruebas de DietaController. Los casos de uso, las peticiones, la dieta y el perfil son simulados. */
@ExtendWith(MockitoExtension.class)
class DietaControllerTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long ID = 20L;
    private static final Long CABALLO_ID = 10L;
    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final BigDecimal MS_TOTAL = new BigDecimal("7.50");
    private static final BigDecimal FORRAJE = new BigDecimal("7.12");
    private static final BigDecimal CONCENTRADO = new BigDecimal("0.38");
    private static final Instant FECHA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private DietaUseCase dietaUseCase;
    @Mock
    private CatalogoUseCase catalogoUseCase;

    @InjectMocks
    private DietaController controller;

    private static Racion racionSimulada() {
        Racion racion = mock(Racion.class);
        lenient().when(racion.msTotalKg()).thenReturn(MS_TOTAL);
        lenient().when(racion.forrajeKg()).thenReturn(FORRAJE);
        lenient().when(racion.concentradoKg()).thenReturn(CONCENTRADO);
        return racion;
    }

    private static PerfilDieta perfilSimulado(boolean inferido) {
        RequerimientoNrc requerimiento = mock(RequerimientoNrc.class);
        lenient().when(requerimiento.pesoReferenciaKg()).thenReturn(PESO);
        lenient().when(requerimiento.energiaDigestibleMcalDia()).thenReturn(new BigDecimal("16.70"));
        lenient().when(requerimiento.proteinaCrudaGDia()).thenReturn(new BigDecimal("630.00"));
        lenient().when(requerimiento.calcioGDia()).thenReturn(new BigDecimal("20.00"));
        lenient().when(requerimiento.fosforoGDia()).thenReturn(new BigDecimal("14.00"));

        Porcentajes porcentajes = mock(Porcentajes.class);
        lenient().when(porcentajes.msDelPeso()).thenReturn(new BigDecimal("1.5"));
        lenient().when(porcentajes.forraje()).thenReturn(new BigDecimal("95"));
        lenient().when(porcentajes.concentrado()).thenReturn(new BigDecimal("5"));

        PerfilDieta perfil = mock(PerfilDieta.class);
        lenient().when(perfil.id()).thenReturn(5L);
        lenient().when(perfil.categoria()).thenReturn("adulto");
        lenient().when(perfil.estadoSalud()).thenReturn("laminitis");
        lenient().when(perfil.nombreEstado()).thenReturn("Laminitis");
        lenient().when(perfil.fuente()).thenReturn("documentado");
        lenient().when(perfil.esInferido()).thenReturn(inferido);
        lenient().when(perfil.criterioNutricional()).thenReturn("Bajo en almidón");
        lenient().when(perfil.criterioRacion()).thenReturn("1.5% del peso corporal en MS/día");
        lenient().when(perfil.manejoAgua()).thenReturn("Agua limpia");
        lenient().when(perfil.requerimiento()).thenReturn(requerimiento);
        lenient().when(perfil.porcentajes()).thenReturn(porcentajes);
        lenient().when(perfil.items(any(TipoItem.class))).thenReturn(List.of());
        lenient().when(perfil.items(TipoItem.FORRAJE_BASE)).thenReturn(List.of("Heno"));
        return perfil;
    }

    private static DietaDetallada detalleSimulado(boolean inferido) {
        Racion racion = racionSimulada();
        Dieta dieta = mock(Dieta.class);
        lenient().when(dieta.id()).thenReturn(ID);
        lenient().when(dieta.caballoId()).thenReturn(CABALLO_ID);
        lenient().when(dieta.caballoNombre()).thenReturn("Trueno");
        lenient().when(dieta.caballoCodigo()).thenReturn("EQ-001");
        lenient().when(dieta.categoria()).thenReturn("adulto");
        lenient().when(dieta.estadoSalud()).thenReturn("laminitis");
        lenient().when(dieta.pesoKg()).thenReturn(PESO);
        lenient().when(dieta.racion()).thenReturn(racion);
        lenient().when(dieta.observaciones()).thenReturn("Sin novedad");
        lenient().when(dieta.creadoEn()).thenReturn(FECHA);

        PerfilDieta perfil = perfilSimulado(inferido);
        DietaDetallada detalle = mock(DietaDetallada.class);
        lenient().when(detalle.dieta()).thenReturn(dieta);
        lenient().when(detalle.perfil()).thenReturn(perfil);
        return detalle;
    }

    private static UsuarioAutenticado actualSimulado() {
        UsuarioAutenticado actual = mock(UsuarioAutenticado.class);
        lenient().when(actual.id()).thenReturn(USUARIO_ID);
        return actual;
    }

    private static void verificarDieta(DietaResponse respuesta) {
        assertEquals(ID, respuesta.id());
        assertEquals(CABALLO_ID, respuesta.caballoId());
        assertEquals("Trueno", respuesta.caballoNombre());
        assertEquals("EQ-001", respuesta.caballoCodigo());
        assertEquals(PESO, respuesta.pesoKg());
        assertEquals(MS_TOTAL, respuesta.racion().materiaSecaTotalKgDia());
        assertEquals(FORRAJE, respuesta.racion().forrajeKgDia());
        assertEquals(CONCENTRADO, respuesta.racion().concentradoKgDia());
        assertEquals("1.5% del peso corporal en MS/día", respuesta.racion().criterio());
        assertEquals(List.of("Heno"), respuesta.perfil().forrajeBase());
        assertEquals("Sin novedad", respuesta.observaciones());
    }

    @Test
    void generarRespondeCreadoConLaDieta() {
        //Arrange
        GenerarDietaRequest request = mock(GenerarDietaRequest.class);
        lenient().when(request.caballoId()).thenReturn(CABALLO_ID);
        lenient().when(request.observaciones()).thenReturn("Sin novedad");
        DietaDetallada detalle = detalleSimulado(false);
        when(dietaUseCase.generar(USUARIO_ID, CABALLO_ID, "Sin novedad")).thenReturn(detalle);

        //Act
        ResponseEntity<DietaResponse> respuesta = controller.generar(request, actualSimulado());

        //Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verificarDieta(respuesta.getBody());
    }

    @Test
    void lasPeticionesEntreganSusDatosAlCasoDeUso() {
        //Arrange
        GenerarDietaRequest generar = new GenerarDietaRequest(CABALLO_ID, "Primera nota");
        ObservacionesRequest observaciones = new ObservacionesRequest("Segunda nota");
        VistaPreviaRequest vistaPrevia = new VistaPreviaRequest("adulto", "laminitis", PESO);
        DietaDetallada detalle = detalleSimulado(false);
        Racion racion = racionSimulada();
        PerfilDieta perfil = perfilSimulado(false);
        VistaPrevia vista = mock(VistaPrevia.class);
        lenient().when(vista.racion()).thenReturn(racion);
        lenient().when(vista.perfil()).thenReturn(perfil);
        UsuarioAutenticado actual = actualSimulado();
        when(dietaUseCase.generar(USUARIO_ID, CABALLO_ID, "Primera nota")).thenReturn(detalle);
        when(dietaUseCase.actualizarObservaciones(ID, USUARIO_ID, "Segunda nota")).thenReturn(detalle);
        when(dietaUseCase.vistaPrevia("adulto", "laminitis", PESO)).thenReturn(vista);

        //Act
        controller.generar(generar, actual);
        controller.actualizar(ID, observaciones, actual);
        controller.vistaPrevia(vistaPrevia);

        //Assert
        verify(dietaUseCase).generar(USUARIO_ID, CABALLO_ID, "Primera nota");
        verify(dietaUseCase).actualizarObservaciones(ID, USUARIO_ID, "Segunda nota");
        verify(dietaUseCase).vistaPrevia("adulto", "laminitis", PESO);
    }

    @Test
    void obtenerDevuelveLaDietaConElAvisoGeneral() {
        //Arrange
        DietaDetallada detalle = detalleSimulado(false);
        when(dietaUseCase.obtener(ID, USUARIO_ID)).thenReturn(detalle);

        //Act
        DietaResponse respuesta = controller.obtener(ID, actualSimulado());

        //Assert
        verificarDieta(respuesta);
        assertEquals(Avisos.GENERAL, respuesta.aviso());
        assertFalse(respuesta.perfil().inferido());
    }

    @Test
    void unPerfilInferidoAgregaLaAdvertenciaAlAviso() {
        //Arrange
        DietaDetallada detalle = detalleSimulado(true);
        when(dietaUseCase.obtener(ID, USUARIO_ID)).thenReturn(detalle);

        //Act
        DietaResponse respuesta = controller.obtener(ID, actualSimulado());

        //Assert
        assertEquals(Avisos.GENERAL + Avisos.PERFIL_INFERIDO, respuesta.aviso());
        assertTrue(respuesta.perfil().inferido());
    }

    @Test
    void obtenerUnaDietaInexistenteDejaPasarLaExcepcion() {
        //Arrange
        UsuarioAutenticado actual = actualSimulado();
        when(dietaUseCase.obtener(ID, USUARIO_ID)).thenThrow(new RecursoNoEncontradoException("No existe"));

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> controller.obtener(ID, actual));
    }

    @Test
    void listarConvierteLaPaginaDeDietas() {
        //Arrange
        Pagina<DietaDetallada> pagina = new Pagina<>(List.of(detalleSimulado(false)), 0, 10, 1L, 1);
        when(dietaUseCase.listar(USUARIO_ID, 0, 10)).thenReturn(pagina);

        //Act
        PaginaResponse<DietaResponse> respuesta = controller.listar(0, 10, actualSimulado());

        //Assert
        assertEquals(1, respuesta.contenido().size());
        verificarDieta(respuesta.contenido().get(0));
        assertEquals(0, respuesta.pagina());
        assertEquals(10, respuesta.tamano());
        assertEquals(1L, respuesta.totalElementos());
        assertEquals(1, respuesta.totalPaginas());
    }

    @Test
    void actualizarEnviaLasObservacionesDeLaPeticion() {
        //Arrange
        ObservacionesRequest request = mock(ObservacionesRequest.class);
        lenient().when(request.observaciones()).thenReturn("Nueva nota");
        DietaDetallada detalle = detalleSimulado(false);
        when(dietaUseCase.actualizarObservaciones(ID, USUARIO_ID, "Nueva nota")).thenReturn(detalle);

        //Act
        DietaResponse respuesta = controller.actualizar(ID, request, actualSimulado());

        //Assert
        verificarDieta(respuesta);
    }

    @Test
    void eliminarRespondeSinContenido() {
        //Act
        ResponseEntity<Void> respuesta = controller.eliminar(ID, actualSimulado());

        //Assert
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(dietaUseCase).eliminar(ID, USUARIO_ID);
    }

    @Test
    void vistaPreviaCalculaSinGuardar() {
        //Arrange
        VistaPreviaRequest request = mock(VistaPreviaRequest.class);
        lenient().when(request.categoria()).thenReturn("adulto");
        lenient().when(request.estadoSalud()).thenReturn("laminitis");
        lenient().when(request.pesoKg()).thenReturn(PESO);
        Racion racion = racionSimulada();
        PerfilDieta perfil = perfilSimulado(false);
        VistaPrevia vista = mock(VistaPrevia.class);
        lenient().when(vista.categoria()).thenReturn("adulto");
        lenient().when(vista.estadoSalud()).thenReturn("laminitis");
        lenient().when(vista.pesoKg()).thenReturn(PESO);
        lenient().when(vista.racion()).thenReturn(racion);
        lenient().when(vista.perfil()).thenReturn(perfil);
        when(dietaUseCase.vistaPrevia("adulto", "laminitis", PESO)).thenReturn(vista);

        //Act
        VistaPreviaResponse respuesta = controller.vistaPrevia(request);

        //Assert
        assertEquals("adulto", respuesta.categoria());
        assertEquals("laminitis", respuesta.estadoSalud());
        assertEquals(PESO, respuesta.pesoKg());
        assertEquals(MS_TOTAL, respuesta.racion().materiaSecaTotalKgDia());
        assertEquals("Laminitis", respuesta.perfil().nombreEstado());
        assertEquals(Avisos.GENERAL, respuesta.aviso());
    }

    @Test
    void regenerarRespondeCreadoConLaDietaNueva() {
        //Arrange
        DietaDetallada detalle = detalleSimulado(false);
        when(dietaUseCase.regenerar(ID, USUARIO_ID)).thenReturn(detalle);

        //Act
        ResponseEntity<DietaResponse> respuesta = controller.regenerar(ID, actualSimulado());

        //Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verificarDieta(respuesta.getBody());
    }

    @Test
    void vigenteDevuelveLaDietaMasReciente() {
        //Arrange
        DietaDetallada detalle = detalleSimulado(false);
        when(dietaUseCase.vigente(USUARIO_ID, CABALLO_ID)).thenReturn(detalle);

        //Act
        DietaResponse respuesta = controller.vigente(CABALLO_ID, actualSimulado());

        //Assert
        verificarDieta(respuesta);
    }

    @Test
    void historialConvierteLaPaginaDelCaballo() {
        //Arrange
        Pagina<DietaDetallada> pagina = new Pagina<>(List.of(detalleSimulado(false)), 1, 5, 6L, 2);
        when(dietaUseCase.historial(USUARIO_ID, CABALLO_ID, 1, 5)).thenReturn(pagina);

        //Act
        PaginaResponse<DietaResponse> respuesta = controller.historial(CABALLO_ID, 1, 5, actualSimulado());

        //Assert
        verificarDieta(respuesta.contenido().get(0));
        assertEquals(1, respuesta.pagina());
        assertEquals(5, respuesta.tamano());
        assertEquals(6L, respuesta.totalElementos());
        assertEquals(2, respuesta.totalPaginas());
    }

    @Test
    void perfilesDevuelveElCatalogoFiltrado() {
        //Arrange
        PerfilDieta perfil = perfilSimulado(false);
        when(catalogoUseCase.perfiles("adulto", "laminitis")).thenReturn(List.of(perfil));

        //Act
        List<PerfilResponse> respuesta = controller.perfiles("adulto", "laminitis");

        //Assert
        assertEquals(1, respuesta.size());
        assertEquals(5L, respuesta.get(0).id());
        assertEquals("adulto", respuesta.get(0).categoria());
        assertEquals(PESO, respuesta.get(0).requerimientoNrcReferencia().pesoReferenciaKg());
        assertEquals(new BigDecimal("95"), respuesta.get(0).porcentajeForraje());
        assertEquals(List.of("Heno"), respuesta.get(0).forrajeBase());
        assertTrue(respuesta.get(0).advertencias().isEmpty());
    }
}
