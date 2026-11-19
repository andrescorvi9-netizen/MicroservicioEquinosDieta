package com.equinos.registrocaballo.domain.model;

import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

import lombok.Getter;

/**
 * Caballo registrado por un usuario.
 * El borrado es lógico (activo = false) y el código es único entre los caballos activos.
 */
@Getter
public class Caballo {

    private final Long id;
    private final Long usuarioId;
    private String nombre;
    private String codigo;
    private BigDecimal pesoKg;
    private Categoria categoria;
    private EstadoSalud estadoSalud;
    private boolean activo;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    public Caballo(Long id, Long usuarioId, String nombre, String codigo, BigDecimal pesoKg, Categoria categoria,
                   EstadoSalud estadoSalud, boolean activo, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.codigo = codigo;
        this.pesoKg = pesoKg;
        this.categoria = categoria;
        this.estadoSalud = estadoSalud;
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static Caballo nuevo(Long usuarioId, String nombre, String codigo, BigDecimal pesoKg, Categoria categoria,
                                EstadoSalud estadoSalud, Instant ahora) {
        return new Caballo(null, usuarioId, nombre.trim(), normalizarCodigo(codigo), pesoValido(pesoKg), categoria,
                estadoSalud, true, ahora, ahora);
    }

    /** Los códigos se comparan sin espacios y en mayúsculas. */
    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal pesoValido(BigDecimal pesoKg) {
        if (pesoKg == null || pesoKg.signum() <= 0) {
            throw new ReglaNegocioException("El peso debe ser mayor que cero");
        }
        return pesoKg;
    }

    public void actualizar(String nuevoNombre, String nuevoCodigo, BigDecimal nuevoPesoKg, Categoria nuevaCategoria,
                           EstadoSalud nuevoEstadoSalud, Instant ahora) {
        this.nombre = nuevoNombre.trim();
        this.codigo = normalizarCodigo(nuevoCodigo);
        this.pesoKg = pesoValido(nuevoPesoKg);
        this.categoria = nuevaCategoria;
        this.estadoSalud = nuevoEstadoSalud;
        this.actualizadoEn = ahora;
    }

    public void cambiarPeso(BigDecimal nuevoPesoKg, Instant ahora) {
        this.pesoKg = pesoValido(nuevoPesoKg);
        this.actualizadoEn = ahora;
    }

    public void cambiarEstadoSalud(EstadoSalud nuevoEstadoSalud, Instant ahora) {
        this.estadoSalud = nuevoEstadoSalud;
        this.actualizadoEn = ahora;
    }

    public void desactivar(Instant ahora) {
        this.activo = false;
        this.actualizadoEn = ahora;
    }

    public void reactivar(Instant ahora) {
        this.activo = true;
        this.actualizadoEn = ahora;
    }

}
