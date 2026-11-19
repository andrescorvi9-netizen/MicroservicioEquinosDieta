package com.equinos.registrocaballo.domain.model;

/** Usuario autenticado que registra un caballo. Sus datos vienen del JWT. */
public record Propietario(Long id, String nombre, String correo) {
}
