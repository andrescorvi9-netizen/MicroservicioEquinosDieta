package com.equinos.dieta.infraestructure.entry_points;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

/** Formato común de error */
public record ErrorApi(OffsetDateTime timestamp, int status, String error, String mensaje, String ruta,
                       List<String> detalles) {

    public static ErrorApi de(HttpStatus status, String mensaje, String ruta, List<String> detalles) {
        return new ErrorApi(HoraColombia.de(Instant.now()), status.value(), status.getReasonPhrase(), mensaje, ruta, detalles);
    }
}
