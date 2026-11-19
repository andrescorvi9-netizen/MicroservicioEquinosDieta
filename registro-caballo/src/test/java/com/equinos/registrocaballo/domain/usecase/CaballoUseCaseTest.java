package com.equinos.registrocaballo.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.registrocaballo.domain.exception.ConflictoException;
import com.equinos.registrocaballo.domain.exception.RecursoNoEncontradoException;
import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;
import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.DatosCaballo;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;
import com.equinos.registrocaballo.domain.model.Propietario;
import com.equinos.registrocaballo.domain.model.gateway.CaballoGateway;
import com.equinos.registrocaballo.domain.model.gateway.NotificacionesGateway;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class CaballoUseCaseTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long OTRO_USUARIO_ID = 2L;
    private static final Long ID = 10L;
    private static final BigDecimal PESO = new BigDecimal("500.00");

    @Mock
    private CaballoGateway caballos;
    @Mock
    private NotificacionesGateway notificaciones;

    @InjectMocks
    private CaballoUseCase useCase;

    
    private static DatosCaballo datosSimulados(String codigo, String categoria, String estado) {
        DatosCaballo datos = mock(DatosCaballo.class);
        lenient().when(datos.nombre()).thenReturn(" Trueno ");
        lenient().when(datos.codigo()).thenReturn(codigo);
        lenient().when(datos.pesoKg()).thenReturn(PESO);
        lenient().when(datos.categoria()).thenReturn(categoria);
        lenient().when(datos.estadoSalud()).thenReturn(estado);
        return datos;
    }

    /** Caballo simulado del usuario, con código EQ-001. */
    private static Caballo caballoSimulado(boolean activo) {
        Caballo caballo = mock(Caballo.class);
        lenient().when(caballo.getId()).thenReturn(ID);
        lenient().when(caballo.getUsuarioId()).thenReturn(USUARIO_ID);
        lenient().when(caballo.getNombre()).thenReturn("Trueno");
        lenient().when(caballo.getCodigo()).thenReturn("EQ-001");
        lenient().when(caballo.isActivo()).thenReturn(activo);
        return caballo;
    }

    @SuppressWarnings("unchecked")
    private static Pagina<Caballo> paginaSimulada(long total) {
        Pagina<Caballo> pagina = mock(Pagina.class);
        lenient().when(pagina.totalElementos()).thenReturn(total);
        return pagina;
    }

    @Test
    void registrarNormalizaElCodigoYDejaElCaballoActivo() {
        //Arrange
        Propietario propietario = mock(Propietario.class);
        DatosCaballo datos = datosSimulados(" eq-001 ", "ADULTO", "laminitis");
        Caballo caballoGuardado = mock(Caballo.class);

        lenient().when(propietario.id()).thenReturn(USUARIO_ID);
        when(caballos.existeCodigoActivo("EQ-001")).thenReturn(false);
        when(caballos.guardar(any(Caballo.class))).thenReturn(caballoGuardado);

        //Act
        Caballo caballoResultado = useCase.registrar(propietario, datos);

        //Assert
        assertSame(caballoGuardado, caballoResultado);
        ArgumentCaptor<Caballo> enviado = ArgumentCaptor.forClass(Caballo.class);
        verify(caballos).guardar(enviado.capture());
        assertEquals("EQ-001", enviado.getValue().getCodigo());
        assertEquals("Trueno", enviado.getValue().getNombre());
        assertEquals(USUARIO_ID, enviado.getValue().getUsuarioId());
        assertEquals(Categoria.ADULTO, enviado.getValue().getCategoria());
        assertEquals(EstadoSalud.LAMINITIS, enviado.getValue().getEstadoSalud());
        assertTrue(enviado.getValue().isActivo());
    }

    @Test
    void elCodigoNoSePuedeRepetirAunqueSeaDeOtroUsuario() {
        //Arrange
        Propietario propietario = mock(Propietario.class);
        DatosCaballo datos = datosSimulados("eq-001", "adulto", "laminitis");
        when(caballos.existeCodigoActivo("EQ-001")).thenReturn(true);

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.registrar(propietario, datos));
        verify(caballos, never()).guardar(any());
    }

    @Test
    void categoriaOEstadoInvalidosSeRechazanSinGuardar() {
        //Arrange
        Propietario propietario = mock(Propietario.class);
        DatosCaballo categoriaMala = datosSimulados("EQ-001", "anciano", "laminitis");
        DatosCaballo estadoMalo = datosSimulados("EQ-001", "adulto", "gripe");
        when(caballos.existeCodigoActivo(anyString())).thenReturn(false);

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.registrar(propietario, categoriaMala));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrar(propietario, estadoMalo));
        verify(caballos, never()).guardar(any());
    }

    @Test
    void obtenerDevuelveElCaballoActivoDelUsuario() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));

        //Act
        Caballo caballoResultado = useCase.obtener(ID, USUARIO_ID);

        //Assert
        assertSame(caballo, caballoResultado);
    }

    @Test
    void unCaballoAjenoBorradoOInexistenteSeVeComoNoEncontrado() {
        //Arrange
        when(caballos.buscarActivo(ID, OTRO_USUARIO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.obtener(ID, OTRO_USUARIO_ID));
    }

    @Test
    void listarPideLaPaginaAlGateway() {
        //Arrange
        Pagina<Caballo> pagina = paginaSimulada(1);
        when(caballos.listarActivos(USUARIO_ID, 0, 10)).thenReturn(pagina);

        //Act
        Pagina<Caballo> paginaResultado = useCase.listar(USUARIO_ID, 0, 10);

        //Assert
        assertSame(pagina, paginaResultado);
        assertEquals(1, paginaResultado.totalElementos());
    }

    @Test
    void unaPaginacionNoValidaSeRechazaSinConsultar() {
        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, -1, 10));
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, 0, 0));
        assertThrows(ReglaNegocioException.class, () -> useCase.listar(USUARIO_ID, 0, 101));
        assertThrows(ReglaNegocioException.class, () -> useCase.buscar(USUARIO_ID, null, null, 0, 101));
        verifyNoInteractions(caballos);
    }

    @Test
    void actualizarConservandoElCodigoPropioNoConsultaSiEstaEnUso() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        DatosCaballo datos = datosSimulados("eq-001", "adulto", "laminitis");
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        Caballo caballoResultado = useCase.actualizar(ID, USUARIO_ID, datos);

        //Assert
        assertSame(caballo, caballoResultado);
        verify(caballos, never()).existeCodigoActivo(anyString());
        verify(caballo).actualizar(eq(" Trueno "), eq("EQ-001"), eq(PESO), eq(Categoria.ADULTO),
                eq(EstadoSalud.LAMINITIS), any(Instant.class));
    }

    @Test
    void actualizarPuedeCambiarAUnCodigoLibre() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        DatosCaballo datos = datosSimulados("eq-009", "joven", "colicos");
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.existeCodigoActivo("EQ-009")).thenReturn(false);
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        useCase.actualizar(ID, USUARIO_ID, datos);

        //Assert
        verify(caballo).actualizar(eq(" Trueno "), eq("EQ-009"), eq(PESO), eq(Categoria.JOVEN),
                eq(EstadoSalud.COLICOS), any(Instant.class));
        verify(caballos).guardar(caballo);
    }

    @Test
    void actualizarAUnCodigoQueYaUsaOtroCaballoEsConflicto() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        DatosCaballo datos = datosSimulados("EQ-002", "adulto", "laminitis");
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.existeCodigoActivo("EQ-002")).thenReturn(true);

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.actualizar(ID, USUARIO_ID, datos));
        verify(caballos, never()).guardar(any());
    }

    @Test
    void unCaballoDadoDeBajaNoSePuedeModificarNiEliminar() {
        //Arrange: el gateway no encuentra un caballo activo con ese id
        DatosCaballo datos = datosSimulados("EQ-001", "adulto", "laminitis");
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.actualizar(ID, USUARIO_ID, datos));
        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.actualizarPeso(ID, USUARIO_ID, new BigDecimal("400")));
        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.actualizarEstadoSalud(ID, USUARIO_ID, "colicos"));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.eliminar(ID, USUARIO_ID));
        verify(caballos, never()).guardar(any());
    }

    @Test
    void eliminarEsUnaBajaLogica() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));

        //Act
        useCase.eliminar(ID, USUARIO_ID);

        //Assert: no se borra, se desactiva y se guarda
        verify(caballo).desactivar(any(Instant.class));
        verify(caballos).guardar(caballo);
    }

    @Test
    void reactivarUnCaballoDadoDeBaja() {
        //Arrange
        Caballo caballo = caballoSimulado(false);
        when(caballos.buscarCualquiera(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.existeCodigoActivo("EQ-001")).thenReturn(false);
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        Caballo caballoResultado = useCase.reactivar(ID, USUARIO_ID);

        //Assert
        assertSame(caballo, caballoResultado);
        verify(caballo).reactivar(any(Instant.class));
    }

    @Test
    void reactivarUnCaballoInexistenteOAjenoEsNoEncontrado() {
        //Arrange
        when(caballos.buscarCualquiera(ID, USUARIO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.reactivar(ID, USUARIO_ID));
    }

    @Test
    void reactivarUnCaballoQueYaEstaActivoEsConflicto() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarCualquiera(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.reactivar(ID, USUARIO_ID));
        verify(caballo, never()).reactivar(any());
        verify(caballos, never()).guardar(any());
    }

    @Test
    void noSeReactivaSiOtroCaballoActivoTomoElCodigo() {
        //Arrange
        Caballo caballo = caballoSimulado(false);
        when(caballos.buscarCualquiera(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.existeCodigoActivo("EQ-001")).thenReturn(true);

        //Act y Assert
        assertThrows(ConflictoException.class, () -> useCase.reactivar(ID, USUARIO_ID));
        verify(caballo, never()).reactivar(any());
        verify(caballos, never()).guardar(any());
    }

    @Test
    void actualizarPesoGuardaElNuevoPeso() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        BigDecimal nuevoPeso = new BigDecimal("455.5");
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        Caballo caballoResultado = useCase.actualizarPeso(ID, USUARIO_ID, nuevoPeso);

        //Assert
        assertSame(caballo, caballoResultado);
        verify(caballo).cambiarPeso(eq(nuevoPeso), any(Instant.class));
    }

    @Test
    void unPesoCeroONegativoSeRechaza() {
        //Arrange: el caballo simulado rechaza el peso, como lo hace la regla del modelo
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        doThrow(new ReglaNegocioException("El peso debe ser mayor que cero"))
                .when(caballo).cambiarPeso(eq(BigDecimal.ZERO), any(Instant.class));

        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.actualizarPeso(ID, USUARIO_ID, BigDecimal.ZERO));
        verify(caballos, never()).guardar(any());
    }

    @Test
    void actualizarEstadoDeSaludAceptaMayusculas() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        useCase.actualizarEstadoSalud(ID, USUARIO_ID, "COLICOS");

        //Assert
        verify(caballo).cambiarEstadoSalud(eq(EstadoSalud.COLICOS), any(Instant.class));
    }

    @Test
    void buscarPorCodigoNormalizaElCodigo() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballos.buscarActivoPorCodigo("EQ-001", USUARIO_ID)).thenReturn(Optional.of(caballo));

        //Act
        Caballo caballoResultado = useCase.buscarPorCodigo(" eq-001 ", USUARIO_ID);

        //Assert
        assertSame(caballo, caballoResultado);
    }

    @Test
    void buscarPorUnCodigoQueNoEsDelUsuarioEsNoEncontrado() {
        //Arrange
        when(caballos.buscarActivoPorCodigo("EQ-001", OTRO_USUARIO_ID)).thenReturn(Optional.empty());

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.buscarPorCodigo("EQ-001", OTRO_USUARIO_ID));
    }

    @Test
    void buscarConvierteLosFiltrosYUnFiltroVacioNoSeAplica() {
        //Arrange
        Pagina<Caballo> adultos = paginaSimulada(1);
        Pagina<Caballo> conColicos = paginaSimulada(0);
        Pagina<Caballo> todos = paginaSimulada(2);
        when(caballos.buscarActivos(eq(USUARIO_ID), eq(Categoria.ADULTO), isNull(), eq(0), eq(10))).thenReturn(adultos);
        when(caballos.buscarActivos(eq(USUARIO_ID), isNull(), eq(EstadoSalud.COLICOS), eq(0), eq(10)))
                .thenReturn(conColicos);
        when(caballos.buscarActivos(eq(USUARIO_ID), isNull(), isNull(), eq(0), eq(10))).thenReturn(todos);

        //Act y Assert
        assertSame(adultos, useCase.buscar(USUARIO_ID, "adulto", null, 0, 10));
        assertSame(conColicos, useCase.buscar(USUARIO_ID, "  ", "colicos", 0, 10));
        assertSame(todos, useCase.buscar(USUARIO_ID, null, "", 0, 10));
    }

    @Test
    void buscarConUnFiltroNoValidoSeRechaza() {
        //Act y Assert
        assertThrows(ReglaNegocioException.class, () -> useCase.buscar(USUARIO_ID, "anciano", null, 0, 10));
        verifyNoInteractions(caballos);
    }

    @Test
    void laBajaInternaDevuelveCuantosCaballosDesactivo() {
        //Arrange
        when(caballos.desactivarActivosDeUsuario(eq(USUARIO_ID), any(Instant.class))).thenReturn(2);

        //Act
        int desactivados = useCase.desactivarTodosDeUsuario(USUARIO_ID);

        //Assert
        assertEquals(2, desactivados);
    }

    @Test
    void cambiarElEstadoDeSaludAvisaAlUsuario() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballo.getEstadoSalud()).thenReturn(EstadoSalud.LAMINITIS);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        useCase.actualizarEstadoSalud(ID, USUARIO_ID, "colicos");

        //Assert
        verify(notificaciones).estadoSaludCambiado(caballo);
    }

    @Test
    void repetirElMismoEstadoDeSaludNoAvisa() {
        //Arrange
        Caballo caballo = caballoSimulado(true);
        when(caballo.getEstadoSalud()).thenReturn(EstadoSalud.COLICOS);
        when(caballos.buscarActivo(ID, USUARIO_ID)).thenReturn(Optional.of(caballo));
        when(caballos.guardar(caballo)).thenReturn(caballo);

        //Act
        useCase.actualizarEstadoSalud(ID, USUARIO_ID, "colicos");

        //Assert
        verifyNoInteractions(notificaciones);
    }
}
