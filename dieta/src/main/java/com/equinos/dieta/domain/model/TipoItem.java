package com.equinos.dieta.domain.model;

import java.util.Locale;

/** Listas que componen un perfil de dieta. El código en minúsculas es el que se guarda en la base. */
public enum TipoItem {
    FORRAJE_BASE, FORRAJE_EVITAR, CONCENTRADO_RECOMENDADO, CONCENTRADO_EVITAR, SUPLEMENTO, REGLA_ALIMENTACION,
    ADVERTENCIA;

    public String codigo() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TipoItem desde(String codigo) {
        return valueOf(codigo.trim().toUpperCase(Locale.ROOT));
    }
}
