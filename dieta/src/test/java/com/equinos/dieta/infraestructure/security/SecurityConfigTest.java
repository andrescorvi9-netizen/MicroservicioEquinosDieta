package com.equinos.dieta.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Pruebas de SecurityConfig. HttpSecurity es simulado y se revisa cada regla que se le configura. */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> sesion;
    @Captor
    private ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> rutas;
    @Captor
    private ArgumentCaptor<Customizer<ExceptionHandlingConfigurer<HttpSecurity>>> errores;

    private final SecurityConfig config = new SecurityConfig();

    private HttpSecurity http;
    private DefaultSecurityFilterChain cadena;

    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registro;
    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl ruta;

    @BeforeEach
    void simularHttpSecurity() throws Exception {
        http = mock(HttpSecurity.class, RETURNS_SELF);
        cadena = mock(DefaultSecurityFilterChain.class);
        lenient().when(http.build()).thenReturn(cadena);
    }

    /** El registro de rutas simulado responde como el real: ruta, regla, ruta, regla... */
    private void simularRegistroDeRutas() {
        registro = mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class,
                invocacion -> invocacion.getMethod().getName().matches("requestMatchers|anyRequest")
                        ? ruta : RETURNS_DEFAULTS.answer(invocacion));
        ruta = mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class,
                invocacion -> invocacion.getMethod().getName().matches("permitAll|authenticated")
                        ? registro : RETURNS_DEFAULTS.answer(invocacion));
    }

    @Test
    void laCadenaAgregaElFiltroJwtAntesDelLoginDeSpring() throws Exception {
        //Act
        SecurityFilterChain resultado = config.securityFilterChain(http, jwtService);

        //Assert
        assertSame(cadena, resultado);
        verify(http).addFilterBefore(any(JwtAuthenticationFilter.class),
                eq(UsernamePasswordAuthenticationFilter.class));
    }

    @Test
    void laSesionNoGuardaEstado() throws Exception {
        //Arrange
        SessionManagementConfigurer<HttpSecurity> configurador = mock(SessionManagementConfigurer.class);

        //Act
        config.securityFilterChain(http, jwtService);
        verify(http).sessionManagement(sesion.capture());
        sesion.getValue().customize(configurador);

        //Assert
        verify(configurador).sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Test
    void lasRutasInternasNoUsanToken() throws Exception {
        //Arrange
        simularRegistroDeRutas();

        //Act
        config.securityFilterChain(http, jwtService);
        verify(http).authorizeHttpRequests(rutas.capture());
        rutas.getValue().customize(registro);

        //Assert
        verify(registro).requestMatchers("/error");
        verify(registro).requestMatchers("/api/internal/**");
        verify(registro).anyRequest();
        verify(ruta).authenticated();
    }

    @Test
    void losErroresDeAutenticacionRespondenEnJson() throws Exception {
        //Arrange
        ExceptionHandlingConfigurer<HttpSecurity> configurador = mock(ExceptionHandlingConfigurer.class);

        //Act
        config.securityFilterChain(http, jwtService);
        verify(http).exceptionHandling(errores.capture());
        errores.getValue().customize(configurador);

        //Assert
        verify(configurador).authenticationEntryPoint(any(JsonAuthenticationEntryPoint.class));
    }

    @Test
    void noExisteUnUsuarioPorDefecto() {
        //Arrange
        UserDetailsService servicio = config.userDetailsService();

        //Act y Assert
        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("ana"));
    }
}
