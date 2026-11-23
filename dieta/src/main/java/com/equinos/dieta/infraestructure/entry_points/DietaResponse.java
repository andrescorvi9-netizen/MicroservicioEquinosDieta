package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.model.DietaDetallada;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DietaResponse(Long id, Long caballoId, String caballoNombre, String caballoCodigo, String categoria,
                            String estadoSalud, BigDecimal pesoKg, RacionResponse racion, PerfilResponse perfil,
                            String observaciones, OffsetDateTime creadoEn, String aviso) {

    public static DietaResponse de(DietaDetallada detalle) {
        return new DietaResponse(detalle.dieta().id(), detalle.dieta().caballoId(), detalle.dieta().caballoNombre(),
                detalle.dieta().caballoCodigo(), detalle.dieta().categoria(), detalle.dieta().estadoSalud(),
                detalle.dieta().pesoKg(), RacionResponse.de(detalle.dieta().racion(), detalle.perfil()),
                PerfilResponse.de(detalle.perfil()), detalle.dieta().observaciones(), HoraColombia.de(detalle.dieta().creadoEn()),
                Avisos.para(detalle.perfil(), detalle.avisoClima()));
    }
}
