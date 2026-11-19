package com.equinos.login.domain.exception;

/** La operación choca con el estado actual de los datos (HTTP 409). */
public class ConflictoException extends DominioException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
