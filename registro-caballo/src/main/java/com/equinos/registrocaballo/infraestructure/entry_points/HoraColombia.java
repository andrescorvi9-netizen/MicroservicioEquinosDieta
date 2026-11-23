package com.equinos.registrocaballo.infraestructure.entry_points;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Las fechas se guardan en UTC y las respuestas las muestran en la hora de Colombia (UTC-5).
 * Colombia no tiene horario de verano, así que el desfase es siempre de -5 horas.
 */
class HoraColombia {

    static final ZoneOffset UTC_MENOS_5 = ZoneOffset.ofHours(-5);

    static OffsetDateTime de(Instant instante) {
        return instante == null ? null : instante.atOffset(UTC_MENOS_5);
    }
}
