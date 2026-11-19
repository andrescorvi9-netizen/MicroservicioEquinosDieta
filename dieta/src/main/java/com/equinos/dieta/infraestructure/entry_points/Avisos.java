package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.model.PerfilDieta;

/** Advertencias que acompañan toda recomendación de dieta. */
public final class Avisos {

    public static final String GENERAL = "Recomendación orientativa basada en reglas documentadas del NRC. "
            + "No es un diagnóstico veterinario ni sustituye la valoración de un profesional en nutrición equina "
            + "o medicina veterinaria.";

    public static final String PERFIL_INFERIDO = " Este perfil es INFERIDO a partir de principios generales: "
            + "las fuentes no incluyen una sección dedicada a esta condición.";

    private Avisos() {
    }

    public static String para(PerfilDieta perfil) {
        return perfil.esInferido() ? GENERAL + PERFIL_INFERIDO : GENERAL;
    }

    /** Agrega el aviso de clima cuando la dieta se acaba de generar y el clima está disponible. */
    public static String para(PerfilDieta perfil, String avisoClima) {
        return avisoClima == null ? para(perfil) : para(perfil) + " " + avisoClima;
    }
}
