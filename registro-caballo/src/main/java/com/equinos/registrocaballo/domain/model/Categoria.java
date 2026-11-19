package com.equinos.registrocaballo.domain.model;

import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Categorías del catálogo NRC. El código en minúsculas es el que se guarda y se expone en la API. */
public enum Categoria {
    POTRILLO, JOVEN, ADULTO;

    public String codigo() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Categoria desde(String valor) {
        String normalizado = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(c -> c.name().equals(normalizado)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Categoría no válida: '" + valor + "'. Valores: "
                        + Arrays.stream(values()).map(Categoria::codigo).collect(Collectors.joining(", "))));
    }
}
