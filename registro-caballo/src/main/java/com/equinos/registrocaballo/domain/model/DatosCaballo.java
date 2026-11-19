package com.equinos.registrocaballo.domain.model;

import java.math.BigDecimal;

/** Datos que llegan para registrar o actualizar un caballo. */
public record DatosCaballo(String nombre, String codigo, BigDecimal pesoKg, String categoria, String estadoSalud) {
}
