package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.ClimaDia;

import java.util.Optional;

public interface ClimaGateway {

    /** Vacío si el clima no está disponible: la dieta se genera igual, sin el aviso de clima. */
    Optional<ClimaDia> climaDelDia();
}
