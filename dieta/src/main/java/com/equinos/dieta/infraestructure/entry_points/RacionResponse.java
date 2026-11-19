package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Racion;

import java.math.BigDecimal;

/** Ración diaria en kg de materia seca. */
public record RacionResponse(BigDecimal materiaSecaTotalKgDia, BigDecimal forrajeKgDia,
                             BigDecimal concentradoKgDia, String criterio) {

    public static RacionResponse de(Racion racion, PerfilDieta perfil) {
        return new RacionResponse(racion.msTotalKg(), racion.forrajeKg(), racion.concentradoKg(),
                perfil.criterioRacion());
    }
}
