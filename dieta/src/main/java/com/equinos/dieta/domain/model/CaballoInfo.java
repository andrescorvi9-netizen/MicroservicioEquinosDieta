package com.equinos.dieta.domain.model;

import java.math.BigDecimal;

/** Datos del caballo que entrega el microservicio registro-caballo. */
public record CaballoInfo(Long id, String nombre, String codigo, BigDecimal pesoKg, String categoria,
                          String estadoSalud) {
}
