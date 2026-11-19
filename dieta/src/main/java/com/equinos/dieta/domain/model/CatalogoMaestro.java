package com.equinos.dieta.domain.model;

import java.util.List;

/** Contenido completo del archivo maestro: 3 categorías, 5 estados de salud y 15 perfiles. */
public record CatalogoMaestro(List<CategoriaCatalogo> categorias, List<EstadoSaludCatalogo> estados,
                              List<PerfilDieta> perfiles) {
}
