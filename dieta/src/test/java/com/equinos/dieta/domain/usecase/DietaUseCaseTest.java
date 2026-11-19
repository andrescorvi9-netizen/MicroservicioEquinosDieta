package com.equinos.dieta.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.exception.RecursoNoEncontradoException;
import com.equinos.dieta.domain.exception.ReglaNegocioException;
import com.equinos.dieta.domain.exception.ServicioNoDisponibleException;
import com.equinos.dieta.domain.model.CaballoInfo;
import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.DietaDetallada;
import com.equinos.dieta.domain.model.Pagina;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Porcentajes;
import com.equinos.dieta.domain.model.VistaPrevia;
import com.equinos.dieta.domain.model.gateway.CaballoGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;
import com.equinos.dieta.domain.model.gateway.DietaGateway;
import com.equinos.dieta.domain.model.ClimaDia;
import com.equinos.dieta.domain.model.gateway.ClimaGateway;
import com.equinos.dieta.domain.model.gateway.NotificacionesGateway;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas de DietaUseCase. Todo es simulado con Mockito: los gateways y también los datos
 * (el caballo que responde registro-caballo, el perfil del catálogo, sus porcentajes y las dietas).
 * La ración sí se calcula de verdad, porque es la regla que se quiere comprobar.
 */
@ExtendWith(MockitoExtension.class)
class DietaUseCaseTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long OTRO_USUARIO_ID = 2L;
    private static final Long CABALLO_ID = 10L;
    private static final Long DIETA_ID = 100L;
    private static final Long PERFIL_ID = 1L;

    @Mock
    private DietaGateway dietas;
    @Mock
    private CatalogoGateway catalogo;
    @Mock
    private CaballoGateway caballos;
    @Mock
    private ClimaGateway clima;
    @Mock
    private NotificacionesGateway notificaciones;

    @InjectMocks
    private DietaUseCase useCase;

    /** Caballo simulado como lo devuelve registro-caballo: adulto con laminitis de 480.5 kg. */
    private static CaballoInfo caballoSimulado() {
        CaballoInfo caballo = mock(CaballoInfo.class);
        lenient().when(caballo.id()).thenReturn(CABALLO_ID);
        lenient().when(caballo.nombre()).thenReturn("Trueno");
        lenient().when(caballo.codigo()).thenReturn("EQ-001");
        lenient().when(caballo.pesoKg()).thenReturn(new BigDecimal("480.5"));
        lenient().when(caballo.categoria()).thenReturn("adulto");
        lenient().when(caballo.estadoSalud()).thenReturn("laminitis");
        return caballo;
    }

    /** Perfil simulado del catálogo: 1.5 % del peso en materia seca, 95 % forraje y 5 % concentrado. */
    private static PerfilDieta perfilSimulado() {
        Porcentajes porcentajes = mock(Porcentajes.class);
        lenient().when(porcentajes.msDelPeso()).thenReturn(new BigDecimal("1.5"));
        lenient().when(porcentajes.forraje()).thenReturn(new BigDecimal("95"));
        lenient().when(porcentajes.concentrado()).thenReturn(new BigDecimal("5"));

        PerfilDieta perfil = mock(PerfilDieta.class);
        lenient().when(perfil.id()).thenReturn(PERFIL_ID);
        lenient().when(perfil.categoria()).thenReturn("adulto");
        lenient().when(perfil.estadoSalud()).thenReturn("laminitis");
        lenient().when(perfil.porcentajes()).thenReturn(porcentajes);
        return perfil;
    }

    /** Dieta guardada simulada, del caballo y con el perfil de arriba. */
    private static Dieta dietaSimulada(String observaciones) {
        Dieta dieta = mock(Dieta.class);
        lenient().when(dieta.id()).thenReturn(DIETA_ID);
        lenient().when(dieta.usuarioId()).thenReturn(USUARIO_ID);
        lenient().when(dieta.caballoId()).thenReturn(CABALLO_ID);
        lenient().when(dieta.perfilId()).thenReturn(PERFIL_ID);
        lenient().when(dieta.observaciones()).thenReturn(observaciones);
        return dieta;
    }

    /**
     * La página se arma con el record real porque el caso de uso llama a su método convertir
     * para detallar cada dieta; lo que va adentro sí son dietas simuladas.
     */
    private static Pagina<Dieta> pagina(Dieta... contenido) {
        return new Pagina<>(List.of(contenido), 0, 10, contenido.length, 1);
    }

    private static void igual(String esperado, BigDecimal obtenido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtenido), "se esperaba " + esperado + ", fue " + obtenido);
    }

    @Test
    void generarCopiaElCaballoYCalculaConSuPesoReal() {
        //Arrange
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        Dieta dietaGuardada = dietaSimulada("Primera dieta");

        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));
        when(dietas.guardar(any(Dieta.class))).thenReturn(dietaGuardada);

        //Act
        DietaDetallada resultado = useCase.generar(USUARIO_ID, CABALLO_ID, "  Primera dieta ");

        //Assert
        assertSame(dietaGuardada, resultado.dieta());
        assertSame(perfil, resultado.perfil());
        ArgumentCaptor<Dieta> enviada = ArgumentCaptor.forClass(Dieta.class);
        verify(dietas).guardar(enviada.capture());
        Dieta d = enviada.getValue();
        assertNull(d.id());
        assertEquals(USUARIO_ID, d.usuarioId());
        assertEquals("Trueno", d.caballoNombre());
        assertEquals("EQ-001", d.caballoCodigo());
        assertEquals(PERFIL_ID, d.perfilId());
        assertEquals("Primera dieta", d.observaciones());
        igual("7.21", d.racion().msTotalKg());
        igual("6.85", d.racion().forrajeKg());
        igual("0.36", d.racion().concentradoKg());
    }

    @Test
    void generarConObservacionesVaciasLasGuardaComoNulas() {
        //Arrange
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));
        when(dietas.guardar(any(Dieta.class))).thenAnswer(inv -> inv.getArgument(0));

        //Act
        DietaDetallada enBlanco = useCase.generar(USUARIO_ID, CABALLO_ID, "   ");
        DietaDetallada nula = useCase.generar(USUARIO_ID, CABALLO_ID, null);

        //Assert
        assertNull(enBlanco.dieta().observaciones());
        assertNull(nula.dieta().observaciones());
    }

    @Test
    void unCaballoQueNoExisteONoEsDelUsuarioDa404() {
        //Arrange
        when(caballos.buscar(999L)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.generar(USUARIO_ID, 999L, null));
        verifyNoInteractions(dietas, catalogo);
    }

    @Test
    void siRegistroCaballoNoRespondeNoSeGeneraLaDieta() {
        //Arrange
        when(caballos.buscar(CABALLO_ID)).thenThrow(new ServicioNoDisponibleException("registro-caballo no responde"));

        //Act y Assert
        assertThrows(ServicioNoDisponibleException.class, () -> useCase.generar(USUARIO_ID, CABALLO_ID, null));
        verifyNoInteractions(dietas);
    }

    @Test
    void unaCombinacionSinPerfilSeRechaza() {
        //Arrange: un potrillo con cólicos no tiene perfil en el catálogo
        CaballoInfo caballo = caballoSimulado();
        lenient().when(caballo.categoria()).thenReturn("potrillo");
        lenient().when(caballo.estadoSalud()).thenReturn("colicos");
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("potrillo", "colicos")).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.generar(USUARIO_ID, CABALLO_ID, null));
        verify(dietas, never()).guardar(any());
    }

    @Test
    void unasObservacionesDeMasDe2000CaracteresSeRechazan() {
        //Arrange
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));

        //Act y Assert
        assertThrows(ReglaNegocioException.class,
                () -> useCase.generar(USUARIO_ID, CABALLO_ID, "x".repeat(2001)));
        verify(dietas, never()).guardar(any());
    }

    @Test
    void obtenerDevuelveLaDietaConSuPerfil() {
        //Arrange
        Dieta dieta = dietaSimulada(null);
        PerfilDieta perfil = perfilSimulado();
        when(dietas.buscar(DIETA_ID, USUARIO_ID)).thenReturn(Optional.of(dieta));
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        DietaDetallada resultado = useCase.obtener(DIETA_ID, USUARIO_ID);

        //Assert
        assertSame(dieta, resultado.dieta());
        assertSame(perfil, resultado.perfil());
    }

    @Test
    void unaDietaAjenaSeVeComoInexistente() {
        //Arrange
        when(dietas.buscar(DIETA_ID, OTRO_USUARIO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtener(DIETA_ID, OTRO_USUARIO_ID));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.regenerar(DIETA_ID, OTRO_USUARIO_ID));
        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.actualizarObservaciones(DIETA_ID, OTRO_USUARIO_ID, "nota"));
        verify(dietas, never()).guardar(any());
    }

    @Test
    void siElPerfilDeUnaDietaYaNoEstaEnElCatalogoSeAvisaDelError() {
        //Arrange
        Dieta dieta = dietaSimulada(null);
        when(dietas.buscar(DIETA_ID, USUARIO_ID)).thenReturn(Optional.of(dieta));
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(IllegalStateException.class, () -> useCase.obtener(DIETA_ID, USUARIO_ID));
    }

    @Test
    void listarDetallaCadaDietaDeLaPagina() {
        //Arrange
        Dieta primera = dietaSimulada(null);
        Dieta segunda = dietaSimulada(null);
        PerfilDieta perfil = perfilSimulado();
        when(dietas.listar(USUARIO_ID, 0, 10)).thenReturn(pagina(primera, segunda));
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        Pagina<DietaDetallada> resultado = useCase.listar(USUARIO_ID, 0, 10);

        //Assert
        assertEquals(2, resultado.totalElementos());
        assertSame(primera, resultado.contenido().get(0).dieta());
        assertSame(segunda, resultado.contenido().get(1).dieta());
        assertSame(perfil, resultado.contenido().get(0).perfil());
    }

    @Test
    void historialDevuelveLasDietasDelCaballo() {
        //Arrange
        Dieta dieta = dietaSimulada(null);
        PerfilDieta perfil = perfilSimulado();
        when(dietas.listarPorCaballo(USUARIO_ID, CABALLO_ID, 0, 10)).thenReturn(pagina(dieta));
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        Pagina<DietaDetallada> resultado = useCase.historial(USUARIO_ID, CABALLO_ID, 0, 10);

        //Assert
        assertEquals(1, resultado.totalElementos());
        assertSame(dieta, resultado.contenido().get(0).dieta());
    }

    @Test
    void unaPaginacionNoValidaSeRechazaSinConsultar() {
        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, -1, 10));
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, 0, 0));
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, 0, 101));
        assertThrows(ReglaNegocioException.class, () -> useCase.historial(USUARIO_ID, CABALLO_ID, -1, 10));
        verifyNoInteractions(dietas);
    }

    @Test
    void vigenteDevuelveLaDietaMasReciente() {
        //Arrange
        Dieta dieta = dietaSimulada(null);
        PerfilDieta perfil = perfilSimulado();
        when(dietas.buscarVigente(USUARIO_ID, CABALLO_ID)).thenReturn(Optional.of(dieta));
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        DietaDetallada resultado = useCase.vigente(USUARIO_ID, CABALLO_ID);

        //Assert
        assertSame(dieta, resultado.dieta());
    }

    @Test
    void unCaballoSinDietasNoTieneVigente() {
        //Arrange
        when(dietas.buscarVigente(USUARIO_ID, CABALLO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.vigente(USUARIO_ID, CABALLO_ID));
    }

    @Test
    void soloSeActualizanLasObservaciones() {
        //Arrange: la dieta simulada devuelve una copia (también simulada) con las observaciones nuevas
        Dieta original = dietaSimulada("primera");
        Dieta conNota = dietaSimulada("segunda");
        PerfilDieta perfil = perfilSimulado();
        when(dietas.buscar(DIETA_ID, USUARIO_ID)).thenReturn(Optional.of(original));
        when(original.conObservaciones("segunda")).thenReturn(conNota);
        when(dietas.guardar(conNota)).thenReturn(conNota);
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        DietaDetallada resultado = useCase.actualizarObservaciones(DIETA_ID, USUARIO_ID, " segunda ");

        //Assert: se guarda la copia con la nota limpia, sin recalcular nada
        assertSame(conNota, resultado.dieta());
        verify(original).conObservaciones("segunda");
        verifyNoInteractions(caballos);
    }

    @Test
    void observacionesEnBlancoSeBorranYLasMuyLargasSeRechazan() {
        //Arrange
        Dieta original = dietaSimulada("primera");
        Dieta sinNota = dietaSimulada(null);
        PerfilDieta perfil = perfilSimulado();
        when(dietas.buscar(DIETA_ID, USUARIO_ID)).thenReturn(Optional.of(original));
        when(original.conObservaciones(isNull())).thenReturn(sinNota);
        when(dietas.guardar(sinNota)).thenReturn(sinNota);
        when(catalogo.buscarPerfilPorId(PERFIL_ID)).thenReturn(Optional.of(perfil));

        //Act
        DietaDetallada resultado = useCase.actualizarObservaciones(DIETA_ID, USUARIO_ID, "   ");

        //Assert
        assertNull(resultado.dieta().observaciones());
        assertThrows(ReglaNegocioException.class,
                () -> useCase.actualizarObservaciones(DIETA_ID, USUARIO_ID, "x".repeat(2001)));
        verify(original, never()).conObservaciones(anyString());
    }

    @Test
    void eliminarBorraLaDietaPropia() {
        //Arrange
        when(dietas.existe(DIETA_ID, USUARIO_ID)).thenReturn(true);

        //Act
        useCase.eliminar(DIETA_ID, USUARIO_ID);

        //Assert
        verify(dietas).eliminar(DIETA_ID);
    }

    @Test
    void eliminarUnaDietaAjenaNoBorraNada() {
        //Arrange
        when(dietas.existe(DIETA_ID, OTRO_USUARIO_ID)).thenReturn(false);

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.eliminar(DIETA_ID, OTRO_USUARIO_ID));
        verify(dietas, never()).eliminar(anyLong());
    }

    @Test
    void vistaPreviaCalculaSinGuardar() {
        //Arrange
        PerfilDieta perfil = perfilSimulado();
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));

        //Act
        VistaPrevia vista = useCase.vistaPrevia(" ADULTO ", "Laminitis", new BigDecimal("500"));

        //Assert
        igual("7.50", vista.racion().msTotalKg());
        igual("7.12", vista.racion().forrajeKg());
        igual("0.38", vista.racion().concentradoKg());
        assertEquals("adulto", vista.categoria());
        assertSame(perfil, vista.perfil());
        verifyNoInteractions(dietas, caballos);
    }

    @Test
    void vistaPreviaSinPerfilSeRechaza() {
        //Arrange
        when(catalogo.buscarPerfil("adulto", "gripe")).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(ReglaNegocioException.class,
                () -> useCase.vistaPrevia("adulto", "gripe", new BigDecimal("400")));
    }

    @Test
    void regenerarCreaUnaDietaNuevaConLasMismasObservaciones() {
        //Arrange
        Dieta anterior = dietaSimulada("nota");
        Dieta nueva = dietaSimulada("nota");
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        when(dietas.buscar(DIETA_ID, USUARIO_ID)).thenReturn(Optional.of(anterior));
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));
        when(dietas.guardar(any(Dieta.class))).thenReturn(nueva);

        //Act
        DietaDetallada resultado = useCase.regenerar(DIETA_ID, USUARIO_ID);

        //Assert: se vuelve a consultar el caballo y se guarda una dieta nueva (sin id)
        assertSame(nueva, resultado.dieta());
        ArgumentCaptor<Dieta> enviada = ArgumentCaptor.forClass(Dieta.class);
        verify(dietas).guardar(enviada.capture());
        assertNull(enviada.getValue().id());
        assertEquals("nota", enviada.getValue().observaciones());
        assertEquals(CABALLO_ID, enviada.getValue().caballoId());
    }

    @Test
    void laLimpiezaInternaDevuelveCuantasDietasBorro() {
        //Arrange
        when(dietas.eliminarPorUsuario(USUARIO_ID)).thenReturn(2L);

        //Act
        long borradas = useCase.eliminarTodasDeUsuario(USUARIO_ID);

        //Assert
        assertEquals(2L, borradas);
    }

    @Test
    void generarAgregaElAvisoDeClimaYNotificaAlUsuario() {
        //Arrange
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        Dieta dietaGuardada = dietaSimulada(null);
        ClimaDia climaDia = mock(ClimaDia.class);
        when(climaDia.aviso()).thenReturn("Aviso de clima");
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));
        when(dietas.guardar(any(Dieta.class))).thenReturn(dietaGuardada);
        when(clima.climaDelDia()).thenReturn(Optional.of(climaDia));

        //Act
        DietaDetallada resultado = useCase.generar(USUARIO_ID, CABALLO_ID, null);

        //Assert
        assertEquals("Aviso de clima", resultado.avisoClima());
        verify(notificaciones).dietaGenerada(dietaGuardada);
    }

    @Test
    void siElClimaNoEstaDisponibleLaDietaSeGeneraSinAviso() {
        //Arrange
        CaballoInfo caballo = caballoSimulado();
        PerfilDieta perfil = perfilSimulado();
        Dieta dietaGuardada = dietaSimulada(null);
        when(caballos.buscar(CABALLO_ID)).thenReturn(Optional.of(caballo));
        when(catalogo.buscarPerfil("adulto", "laminitis")).thenReturn(Optional.of(perfil));
        when(dietas.guardar(any(Dieta.class))).thenReturn(dietaGuardada);
        when(clima.climaDelDia()).thenReturn(Optional.empty());

        //Act
        DietaDetallada resultado = useCase.generar(USUARIO_ID, CABALLO_ID, null);

        //Assert
        assertNull(resultado.avisoClima());
    }
}
