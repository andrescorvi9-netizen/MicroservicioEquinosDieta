package com.equinos.login.domain.exception;

import java.time.Instant;

/** La cuenta está bloqueada temporalmente por intentos fallidos de inicio de sesión (HTTP 423). */
public class CuentaBloqueadaException extends DominioException {

    private final Instant bloqueadoHasta;

    public CuentaBloqueadaException(Instant bloqueadoHasta) {
        super("La cuenta está bloqueada hasta " + bloqueadoHasta);
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
