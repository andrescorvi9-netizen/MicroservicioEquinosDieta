package com.equinos.dieta.domain.exception;

/** Los datos recibidos incumplen una regla de negocio (HTTP 400). */
public class ReglaNegocioException extends DominioException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
