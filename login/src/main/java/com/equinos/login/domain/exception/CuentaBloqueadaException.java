package com.equinos.login.domain.exception;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Cuenta bloqueada (423) */
public class CuentaBloqueadaException extends DominioException {

    private static final DateTimeFormatter HASTA = DateTimeFormatter.ofPattern("yyyy-MM-dd 'a las' HH:mm");

    private final Instant bloqueadoHasta;

    public CuentaBloqueadaException(Instant bloqueadoHasta) {
        super("La cuenta está bloqueada hasta el " + HASTA.format(bloqueadoHasta.atOffset(ZoneOffset.ofHours(-5)))
                + " (hora de Colombia, UTC-5)");
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
