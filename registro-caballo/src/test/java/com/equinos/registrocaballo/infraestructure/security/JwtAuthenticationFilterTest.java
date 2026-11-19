package com.equinos.registrocaballo.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Pruebas de JwtAuthenticationFilter. El servicio JWT, la petición y la respuesta son simulados. */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filtro;

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unTokenValidoAutenticaLaPeticion() throws Exception {
        //Arrange
        UsuarioAutenticado usuario = mock(UsuarioAutenticado.class);
        lenient().when(usuario.token()).thenReturn("token-simulado");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-simulado ");
        when(jwtService.leer("token-simulado")).thenReturn(Optional.of(usuario));

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacion);
        assertSame(usuario, autenticacion.getPrincipal());
        assertEquals("token-simulado", autenticacion.getCredentials());
        verify(chain).doFilter(request, response);
    }

    @Test
    void unTokenInvalidoNoAutenticaPeroLaPeticionContinua() throws Exception {
        //Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer token-malo");
        when(jwtService.leer("token-malo")).thenReturn(Optional.empty());

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void sinEncabezadoNoSeLeeNingunToken() throws Exception {
        //Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
    }

    @Test
    void unEncabezadoQueNoEsBearerSeIgnora() throws Exception {
        //Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        //Act
        filtro.doFilterInternal(request, response, chain);

        //Assert
        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
    }
}
