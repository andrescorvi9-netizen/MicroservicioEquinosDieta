package com.equinos.dieta.domain.model;

import com.equinos.dieta.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ración diaria en kg de materia seca (MS), calculada con el peso real del caballo:
 * <pre>
 * materia seca total = peso x (% del peso corporal)
 * forraje            = materia seca total x (% forraje)
 * concentrado        = materia seca total - forraje
 * </pre>
 * Se redondea a 2 decimales con HALF_EVEN, que es el redondeo con el que se generó el archivo maestro:
 * así las 15 raciones de ejemplo del catálogo se reproducen exactamente.
 */
public record Racion(BigDecimal msTotalKg, BigDecimal forrajeKg, BigDecimal concentradoKg) {

    private static final BigDecimal CIEN = new BigDecimal("100");

    public static Racion calcular(BigDecimal pesoKg, Porcentajes porcentajes) {
        if (pesoKg == null || pesoKg.signum() <= 0) {
            throw new ReglaNegocioException("El peso debe ser mayor que cero");
        }
        BigDecimal msTotal = pesoKg.multiply(porcentajes.msDelPeso()).divide(CIEN, 2, RoundingMode.HALF_EVEN);
        BigDecimal forraje = msTotal.multiply(porcentajes.forraje()).divide(CIEN, 2, RoundingMode.HALF_EVEN);
        return new Racion(msTotal, forraje, msTotal.subtract(forraje));
    }
}
