package com.equinos.registrocaballo.domain.model;

import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Estados de salud del catálogo NRC. El código en minúsculas es el que se guarda y se expone en la API. */
public enum EstadoSalud {
    SIN_COMPLICACIONES, DESHIDRATACION, DESNUTRICION, LAMINITIS, COLICOS;

    public String codigo() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static EstadoSalud desde(String valor) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(e -> e.name().equals(normalizado)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Estado de salud no válido: '" + valor + "'. Valores: "
                        + Arrays.stream(values()).map(EstadoSalud::codigo).collect(Collectors.joining(", "))));
    }
}
