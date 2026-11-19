package com.equinos.dieta.domain.exception;

/** El recurso no existe o no pertenece al usuario autenticado (HTTP 404). */
public class RecursoNoEncontradoException extends DominioException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
