package com.equinos.dieta.domain.model;

import java.util.List;
import java.util.Map;

/** Perfil de dieta del catálogo NRC para una combinación de categoría y estado de salud. */
public record PerfilDieta(Long id, String categoria, String estadoSalud, String nombreEstado, String fuente,
                          String criterioNutricional, RequerimientoNrc requerimiento, Porcentajes porcentajes,
                          String manejoAgua, Map<TipoItem, List<String>> items) {

    public static final String FUENTE_INFERIDO = "inferido";

    /** Los perfiles inferidos no vienen de una sección documentada del NRC y deben advertirse al usuario. */
    public boolean esInferido() {
        return FUENTE_INFERIDO.equals(fuente);
    }

    public List<String> items(TipoItem tipo) {
        return items.getOrDefault(tipo, List.of());
    }

    public String criterioRacion() {
        return porcentajes.msDelPeso().toPlainString() + "% del peso corporal en MS/día ("
                + porcentajes.forraje().stripTrailingZeros().toPlainString() + "% forraje / "
                + porcentajes.concentrado().stripTrailingZeros().toPlainString() + "% concentrado)";
    }
}
