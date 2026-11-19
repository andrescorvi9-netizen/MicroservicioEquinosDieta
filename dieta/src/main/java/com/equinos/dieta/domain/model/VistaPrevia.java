package com.equinos.dieta.domain.model;

import java.math.BigDecimal;

/** Cálculo de una dieta sin guardarla. */
public record VistaPrevia(String categoria, String estadoSalud, BigDecimal pesoKg, Racion racion,
                          PerfilDieta perfil) {
}
