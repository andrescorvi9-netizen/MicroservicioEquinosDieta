package com.equinos.login.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import lombok.Getter;


@Getter
public class Usuario {

    public static final int MAX_INTENTOS_FALLIDOS = 3;
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final Long id;
    private String nombre;
    private String correo;
    private String passwordHash;
    private int intentosFallidos;
    private Instant bloqueadoHasta;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    public Usuario(Long id, String nombre, String correo, String passwordHash, int intentosFallidos,
                   Instant bloqueadoHasta, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static Usuario nuevo(String nombre, String correo, String passwordHash, Instant ahora) {
        return new Usuario(null, nombre.trim(), normalizarCorreo(correo), passwordHash, 0, null, ahora, ahora);
    }


    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    public boolean estaBloqueado(Instant ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
    }


    public boolean registrarIntentoFallido(Instant ahora) {
        intentosFallidos++;
        actualizadoEn = ahora;
        if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
            intentosFallidos = 0;
            return true;
        }
        return false;
    }

    public void registrarIngresoExitoso(Instant ahora) {
        intentosFallidos = 0;
        bloqueadoHasta = null;
        actualizadoEn = ahora;
    }

    public void actualizarDatos(String nuevoNombre, String nuevoCorreo, Instant ahora) {
        this.nombre = nuevoNombre.trim();
        this.correo = normalizarCorreo(nuevoCorreo);
        this.actualizadoEn = ahora;
    }

    public void cambiarPassword(String nuevoHash, Instant ahora) {
        this.passwordHash = nuevoHash;
        this.actualizadoEn = ahora;
    }

}
