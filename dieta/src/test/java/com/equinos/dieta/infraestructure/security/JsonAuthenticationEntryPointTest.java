package com.equinos.dieta.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

/** Pruebas de JsonAuthenticationEntryPoint. La petición y la respuesta son simuladas. */
@ExtendWith(MockitoExtension.class)
class JsonAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private final JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

    @Test
    void respondeNoAutorizadoEnJson() throws Exception {
        //Arrange
        StringWriter cuerpo = new StringWriter();
        when(request.getRequestURI()).thenReturn("/api/privada");
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpo));

        //Act
        entryPoint.commence(request, response, mock(AuthenticationException.class));

        //Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertTrue(cuerpo.toString().contains("\"status\":401"));
        assertTrue(cuerpo.toString().contains("\"mensaje\":\"Token ausente, inválido o vencido\""));
        assertTrue(cuerpo.toString().contains("\"ruta\":\"/api/privada\""));
    }

    @Test
    void escapaLasComillasDeLaRutaParaNoRomperElJson() throws Exception {
        //Arrange
        StringWriter cuerpo = new StringWriter();
        when(request.getRequestURI()).thenReturn("/api/\"x");
        when(response.getWriter()).thenReturn(new PrintWriter(cuerpo));

        //Act
        entryPoint.commence(request, response, mock(AuthenticationException.class));

        //Assert
        assertTrue(cuerpo.toString().contains("\"ruta\":\"/api/\\\"x\""));
    }
}
