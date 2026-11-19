package com.equinos.registrocaballo.domain.model;

import java.util.List;
import java.util.function.Function;

/** Página de resultados independiente de Spring Data. */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public <R> Pagina<R> convertir(Function<T, R> conversion) {
        return new Pagina<>(contenido.stream().map(conversion).toList(), pagina, tamano, totalElementos,
                totalPaginas);
    }
}
