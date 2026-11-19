package com.equinos.dieta.domain.model;

/**
 * Dieta guardada junto con el perfil del catálogo que la originó.
 * El aviso de clima solo existe en la respuesta al generar la dieta; no se guarda en la base.
 */
public record DietaDetallada(Dieta dieta, PerfilDieta perfil, String avisoClima) {

    public DietaDetallada(Dieta dieta, PerfilDieta perfil) {
        this(dieta, perfil, null);
    }
}
