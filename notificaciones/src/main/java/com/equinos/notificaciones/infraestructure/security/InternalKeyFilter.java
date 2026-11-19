package com.equinos.notificaciones.infraestructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protege las rutas internas (/api/internal/**): solo los demás microservicios las pueden llamar
 * y deben traer el encabezado X-Internal-Key con la clave compartida. Las demás rutas no pasan por aquí.
 */
@Component
public class InternalKeyFilter extends OncePerRequestFilter {

    public static final String ENCABEZADO = "X-Internal-Key";

    private final byte[] claveEsperada;

    public InternalKeyFilter(@Value("${app.internal-key}") String claveInterna) {
        this.claveEsperada = claveInterna.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // getServletPath llega decodificada: una ruta como /api/%69nternal/ no puede saltarse el filtro
        return !request.getServletPath().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibida = request.getHeader(ENCABEZADO);
        boolean valida = recibida != null
                && MessageDigest.isEqual(claveEsperada, recibida.getBytes(StandardCharsets.UTF_8));
        if (!valida) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String ruta = request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
            response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":401,"
                    + "\"error\":\"Unauthorized\",\"mensaje\":\"Encabezado X-Internal-Key ausente o incorrecto\","
                    + "\"ruta\":\"" + ruta + "\",\"detalles\":[]}");
            return;
        }
        chain.doFilter(request, response);
    }
}
