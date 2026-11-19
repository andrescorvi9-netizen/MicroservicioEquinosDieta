package com.equinos.registrocaballo.infraestructure.entry_points;

import com.equinos.registrocaballo.domain.model.Pagina;

import java.util.List;

public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> PaginaResponse<T> de(Pagina<T> pagina) {
        return new PaginaResponse<>(pagina.contenido(), pagina.pagina(), pagina.tamano(), pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
