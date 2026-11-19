package com.equinos.dieta.infraestructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad sin estado: cada petición se autentica con el JWT.
 * Las rutas públicas de este servicio se declaran en authorizeHttpRequests.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/error").permitAll()
                        // sin rutas públicas; /api/internal/** no usa JWT: lo protege InternalKeyFilter
                        .requestMatchers("/api/internal/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores.authenticationEntryPoint(new JsonAuthenticationEntryPoint()))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Evita que Spring Boot cree un usuario por defecto: la única autenticación válida es el JWT. */
    @Bean
    public UserDetailsService userDetailsService() {
        return nombreUsuario -> {
            throw new UsernameNotFoundException(nombreUsuario);
        };
    }
}
