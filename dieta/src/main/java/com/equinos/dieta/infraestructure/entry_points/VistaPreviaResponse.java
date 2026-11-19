package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.model.VistaPrevia;

import java.math.BigDecimal;

public record VistaPreviaResponse(String categoria, String estadoSalud, BigDecimal pesoKg, RacionResponse racion,
                                  PerfilResponse perfil, String aviso) {

    public static VistaPreviaResponse de(VistaPrevia vista) {
        return new VistaPreviaResponse(vista.categoria(), vista.estadoSalud(), vista.pesoKg(),
                RacionResponse.de(vista.racion(), vista.perfil()), PerfilResponse.de(vista.perfil()),
                Avisos.para(vista.perfil()));
    }
}
