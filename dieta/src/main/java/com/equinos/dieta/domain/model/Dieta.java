package com.equinos.dieta.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Dieta generada para un caballo. Guarda copia del nombre, el código, la categoría, el estado
 * de salud y el peso que tenía el caballo al generarla: así el historial se consulta sin llamar
 * a registro-caballo y no cambia si después cambia el caballo.
 * Lo único editable son las observaciones.
 */
public record Dieta(Long id, Long usuarioId, Long caballoId, String caballoNombre, String caballoCodigo,
                    Long perfilId, String categoria, String estadoSalud, BigDecimal pesoKg, Racion racion,
                    String observaciones, Instant creadoEn) {

    public Dieta conObservaciones(String nuevasObservaciones) {
        return new Dieta(id, usuarioId, caballoId, caballoNombre, caballoCodigo, perfilId, categoria, estadoSalud,
                pesoKg, racion, nuevasObservaciones, creadoEn);
    }
}
