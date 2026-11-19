package com.equinos.notificaciones.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de InternalKeyFilter. La petición, la respuesta y la cadena de filtros son simuladas. */
@ExtendWith(MockitoExtension.class)
class InternalKeyFilterTest {

    private static final String VALOR_INTERNO = "valor-simulado";
    private static final String RUTA_INTERNA = "/api/internal/recurso/usuario/1";

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private final InternalKeyFilter filtro = new InternalKeyFilter(VALOR_INTERNO);

    @Test
    void lasRutasNormalesNoPasanPorElFiltro() {
        //Arrange
        when(request.getServletPath()).thenReturn("/api/recurso");

        //Act y Assert
        assertTrue(filtro.shouldNotFilter(request));
    }

    @Test
    void lasRutasInternasPasanPorElFiltro() {
        //Arrange
        when(request.getServletPath()).thenReturn(RUTA_INTERNA);

        //Act y Assert
        assertFalse(filtro.shouldNotFilter(request));
    }

    @Test
    void conElEncabezadoCorrectoLaPeticionContinua() throws Exception {
        //Arrange
        when(request.getHeader(InternalKeyFilter.ENCABEZADO)).thenReturn(VALOR_INTERNO);

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void conUnEncabezadoIncorrectoRespondeNoAutorizado() throws Exception {
        //Arrange
        StringWriter cuerpo = new StringWriter();
        when(request.getHeader(InternalKeyFilter.ENCABEZADO)).thenReturn("otro-valor");
        when(request.getRequestURI()).thenReturn(RUTA_INTERNA);
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpo));

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(cuerpo.toString().contains("Encabezado X-Internal-Key ausente o incorrecto"));
        assertTrue(cuerpo.toString().contains("\"ruta\":\"" + RUTA_INTERNA + "\""));
        verifyNoInteractions(chain);
    }

    @Test
    void sinEncabezadoRespondeNoAutorizado() throws Exception {
        //Arrange
        when(request.getHeader(InternalKeyFilter.ENCABEZADO)).thenReturn(null);
        when(request.getRequestURI()).thenReturn(RUTA_INTERNA);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(chain);
    }
}
