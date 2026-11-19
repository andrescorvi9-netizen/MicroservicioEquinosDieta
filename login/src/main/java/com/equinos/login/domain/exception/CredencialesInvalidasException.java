package com.equinos.login.domain.exception;

/** El correo, la contraseña o la cuenta del token no son válidos (HTTP 401). */
public class CredencialesInvalidasException extends DominioException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
