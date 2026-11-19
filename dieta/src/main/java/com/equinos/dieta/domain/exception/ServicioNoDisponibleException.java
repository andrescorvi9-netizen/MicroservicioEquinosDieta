package com.equinos.dieta.domain.exception;

/** Otro microservicio del que depende la operación no respondió (HTTP 503). */
public class ServicioNoDisponibleException extends DominioException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
