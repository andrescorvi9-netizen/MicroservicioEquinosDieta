package com.equinos.registrocaballo.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.registrocaballo.domain.exception.RecursoNoEncontradoException;
import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.DatosCaballo;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;
import com.equinos.registrocaballo.domain.model.Propietario;
import com.equinos.registrocaballo.domain.usecase.CaballoUseCase;
import com.equinos.registrocaballo.infraestructure.security.UsuarioAutenticado;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Pruebas de CaballoController. El caso de uso, las peticiones y el caballo son simulados. */
@ExtendWith(MockitoExtension.class)
class CaballoControllerTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long ID = 10L;
    private static final BigDecimal PESO = new BigDecimal("500.00");
    private static final Instant FECHA = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private CaballoUseCase caballoUseCase;

    @InjectMocks
    private CaballoController controller;

    private static Caballo caballoSimulado() {
        Caballo caballo = mock(Caballo.class);
        lenient().when(caballo.getId()).thenReturn(ID);
        lenient().when(caballo.getNombre()).thenReturn("Trueno");
        lenient().when(caballo.getCodigo()).thenReturn("EQ-001");
        lenient().when(caballo.getPesoKg()).thenReturn(PESO);
        lenient().when(caballo.getCategoria()).thenReturn(Categoria.ADULTO);
        lenient().when(caballo.getEstadoSalud()).thenReturn(EstadoSalud.LAMINITIS);
        lenient().when(caballo.isActivo()).thenReturn(true);
        lenient().when(caballo.getCreadoEn()).thenReturn(FECHA);
        lenient().when(caballo.getActualizadoEn()).thenReturn(FECHA);
        return caballo;
    }

    private static UsuarioAutenticado actualSimulado() {
        UsuarioAutenticado actual = mock(UsuarioAutenticado.class);
        lenient().when(actual.id()).thenReturn(USUARIO_ID);
        lenient().when(actual.nombre()).thenReturn("Ana");
        lenient().when(actual.correo()).thenReturn("ana@correo.com");
        return actual;
    }

    private static CaballoRequest requestSimulado() {
        CaballoRequest request = mock(CaballoRequest.class);
        lenient().when(request.nombre()).thenReturn("Trueno");
        lenient().when(request.codigo()).thenReturn("EQ-001");
        lenient().when(request.pesoKg()).thenReturn(PESO);
        lenient().when(request.categoria()).thenReturn("adulto");
        lenient().when(request.estadoSalud()).thenReturn("laminitis");
        return request;
    }

    private static void verificarCaballo(CaballoResponse respuesta) {
        assertEquals(ID, respuesta.id());
        assertEquals("Trueno", respuesta.nombre());
        assertEquals("EQ-001", respuesta.codigo());
        assertEquals(PESO, respuesta.pesoKg());
        assertEquals("adulto", respuesta.categoria());
        assertEquals("laminitis", respuesta.estadoSalud());
        assertTrue(respuesta.activo());
    }

    @Test
    void registrarRespondeCreadoConElPropietarioDelToken() {
        //Arrange
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.registrar(any(Propietario.class), any(DatosCaballo.class))).thenReturn(caballo);

        //Act
        ResponseEntity<CaballoResponse> respuesta = controller.registrar(requestSimulado(), actualSimulado());

        //Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verificarCaballo(respuesta.getBody());

        ArgumentCaptor<Propietario> propietario = ArgumentCaptor.forClass(Propietario.class);
        ArgumentCaptor<DatosCaballo> datos = ArgumentCaptor.forClass(DatosCaballo.class);
        verify(caballoUseCase).registrar(propietario.capture(), datos.capture());
        assertEquals(USUARIO_ID, propietario.getValue().id());
        assertEquals("ana@correo.com", propietario.getValue().correo());
        assertEquals("EQ-001", datos.getValue().codigo());
        assertEquals(PESO, datos.getValue().pesoKg());
    }

    @Test
    void lasPeticionesEntreganSusDatosAlCasoDeUso() {
        //Arrange
        CaballoRequest caballoRequest = new CaballoRequest("Trueno", "EQ-001", PESO, "adulto", "laminitis");
        EstadoSaludRequest estadoRequest = new EstadoSaludRequest("colicos");
        PesoRequest pesoRequest = new PesoRequest(PESO);
        Caballo caballo = caballoSimulado();
        UsuarioAutenticado actual = actualSimulado();
        when(caballoUseCase.actualizar(eq(ID), eq(USUARIO_ID), any(DatosCaballo.class))).thenReturn(caballo);
        when(caballoUseCase.actualizarEstadoSalud(ID, USUARIO_ID, "colicos")).thenReturn(caballo);
        when(caballoUseCase.actualizarPeso(ID, USUARIO_ID, PESO)).thenReturn(caballo);

        //Act
        controller.actualizar(ID, caballoRequest, actual);
        controller.actualizarEstadoSalud(ID, estadoRequest, actual);
        controller.actualizarPeso(ID, pesoRequest, actual);

        //Assert
        ArgumentCaptor<DatosCaballo> datos = ArgumentCaptor.forClass(DatosCaballo.class);
        verify(caballoUseCase).actualizar(eq(ID), eq(USUARIO_ID), datos.capture());
        assertEquals("EQ-001", datos.getValue().codigo());
        verify(caballoUseCase).actualizarEstadoSalud(ID, USUARIO_ID, "colicos");
        verify(caballoUseCase).actualizarPeso(ID, USUARIO_ID, PESO);
    }

    @Test
    void obtenerDevuelveElCaballoDelUsuario() {
        //Arrange
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.obtener(ID, USUARIO_ID)).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.obtener(ID, actualSimulado());

        //Assert
        verificarCaballo(respuesta);
    }

    @Test
    void obtenerUnCaballoInexistenteDejaPasarLaExcepcion() {
        //Arrange
        UsuarioAutenticado actual = actualSimulado();
        when(caballoUseCase.obtener(ID, USUARIO_ID)).thenThrow(new RecursoNoEncontradoException("No existe"));

        //Act y Assert
        assertThrows(RecursoNoEncontradoException.class, () -> controller.obtener(ID, actual));
    }

    @Test
    void listarConvierteLaPaginaDeCaballos() {
        //Arrange
        Pagina<Caballo> pagina = new Pagina<>(List.of(caballoSimulado()), 0, 10, 1L, 1);
        when(caballoUseCase.listar(USUARIO_ID, 0, 10)).thenReturn(pagina);

        //Act
        PaginaResponse<CaballoResponse> respuesta = controller.listar(0, 10, actualSimulado());

        //Assert
        assertEquals(1, respuesta.contenido().size());
        verificarCaballo(respuesta.contenido().get(0));
        assertEquals(0, respuesta.pagina());
        assertEquals(10, respuesta.tamano());
        assertEquals(1L, respuesta.totalElementos());
        assertEquals(1, respuesta.totalPaginas());
    }

    @Test
    void actualizarEnviaLosDatosDeLaPeticion() {
        //Arrange
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.actualizar(eq(ID), eq(USUARIO_ID), any(DatosCaballo.class))).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.actualizar(ID, requestSimulado(), actualSimulado());

        //Assert
        verificarCaballo(respuesta);
        ArgumentCaptor<DatosCaballo> datos = ArgumentCaptor.forClass(DatosCaballo.class);
        verify(caballoUseCase).actualizar(eq(ID), eq(USUARIO_ID), datos.capture());
        assertEquals("Trueno", datos.getValue().nombre());
        assertEquals("adulto", datos.getValue().categoria());
        assertEquals("laminitis", datos.getValue().estadoSalud());
    }

    @Test
    void eliminarRespondeSinContenido() {
        //Act
        ResponseEntity<Void> respuesta = controller.eliminar(ID, actualSimulado());

        //Assert
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verify(caballoUseCase).eliminar(ID, USUARIO_ID);
    }

    @Test
    void reactivarDevuelveElCaballo() {
        //Arrange
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.reactivar(ID, USUARIO_ID)).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.reactivar(ID, actualSimulado());

        //Assert
        verificarCaballo(respuesta);
    }

    @Test
    void actualizarEstadoSaludEnviaElEstadoDeLaPeticion() {
        //Arrange
        EstadoSaludRequest request = mock(EstadoSaludRequest.class);
        lenient().when(request.estadoSalud()).thenReturn("laminitis");
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.actualizarEstadoSalud(ID, USUARIO_ID, "laminitis")).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.actualizarEstadoSalud(ID, request, actualSimulado());

        //Assert
        verificarCaballo(respuesta);
    }

    @Test
    void actualizarPesoEnviaElPesoDeLaPeticion() {
        //Arrange
        PesoRequest request = mock(PesoRequest.class);
        lenient().when(request.pesoKg()).thenReturn(PESO);
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.actualizarPeso(ID, USUARIO_ID, PESO)).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.actualizarPeso(ID, request, actualSimulado());

        //Assert
        verificarCaballo(respuesta);
    }

    @Test
    void buscarPorCodigoDevuelveElCaballo() {
        //Arrange
        Caballo caballo = caballoSimulado();
        when(caballoUseCase.buscarPorCodigo("EQ-001", USUARIO_ID)).thenReturn(caballo);

        //Act
        CaballoResponse respuesta = controller.buscarPorCodigo("EQ-001", actualSimulado());

        //Assert
        verificarCaballo(respuesta);
    }

    @Test
    void buscarEnviaLosFiltrosYConvierteLaPagina() {
        //Arrange
        Pagina<Caballo> pagina = new Pagina<>(List.of(caballoSimulado()), 1, 5, 6L, 2);
        when(caballoUseCase.buscar(USUARIO_ID, "adulto", "laminitis", 1, 5)).thenReturn(pagina);

        //Act
        PaginaResponse<CaballoResponse> respuesta = controller.buscar("adulto", "laminitis", 1, 5, actualSimulado());

        //Assert
        verificarCaballo(respuesta.contenido().get(0));
        assertEquals(1, respuesta.pagina());
        assertEquals(5, respuesta.tamano());
        assertEquals(6L, respuesta.totalElementos());
        assertEquals(2, respuesta.totalPaginas());
    }
}
