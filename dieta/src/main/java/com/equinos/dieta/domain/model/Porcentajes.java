package com.equinos.dieta.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Porcentajes de la ración de un perfil: materia seca como % del peso corporal y reparto forraje/concentrado.
 * No se escriben en el código: se derivan de las cifras de ejemplo del archivo maestro al cargarlo.
 */
public record Porcentajes(BigDecimal msDelPeso, BigDecimal forraje, BigDecimal concentrado) {

    private static final BigDecimal CIEN = new BigDecimal("100");

    /**
     * Deriva los porcentajes de un ejemplo del catálogo. Como los kilos del archivo vienen redondeados
     * a 2 decimales, la división no es exacta (1,798 % en vez de 1,8 %): el % del peso se redondea
     * a 1 decimal y el reparto a entero.
     */
    public static Porcentajes desdeEjemplo(BigDecimal pesoEjemploKg, BigDecimal msTotalKg, BigDecimal forrajeKg) {
        BigDecimal msDelPeso = msTotalKg.multiply(CIEN).divide(pesoEjemploKg, 1, RoundingMode.HALF_UP);
        BigDecimal forraje = forrajeKg.multiply(CIEN).divide(msTotalKg, 0, RoundingMode.HALF_UP);
        return new Porcentajes(msDelPeso, forraje, CIEN.subtract(forraje));
    }
}
