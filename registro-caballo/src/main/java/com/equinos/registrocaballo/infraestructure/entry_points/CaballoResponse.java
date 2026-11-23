package com.equinos.registrocaballo.infraestructure.entry_points;

import com.equinos.registrocaballo.domain.model.Caballo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CaballoResponse(Long id, String nombre, String codigo, BigDecimal pesoKg, String categoria,
                              String estadoSalud, boolean activo, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {

    public static CaballoResponse de(Caballo caballo) {
        return new CaballoResponse(caballo.getId(), caballo.getNombre(), caballo.getCodigo(), caballo.getPesoKg(),
                caballo.getCategoria().codigo(), caballo.getEstadoSalud().codigo(), caballo.isActivo(),
                HoraColombia.de(caballo.getCreadoEn()), HoraColombia.de(caballo.getActualizadoEn()));
    }
}
