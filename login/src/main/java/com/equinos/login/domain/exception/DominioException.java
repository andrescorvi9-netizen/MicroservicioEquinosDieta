package com.equinos.login.domain.exception;

/** Base de las excepciones de negocio. No depende de ningún framework. */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensaje) {
        super(mensaje);
    }
}
