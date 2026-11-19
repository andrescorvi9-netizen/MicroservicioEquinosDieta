package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.CaballoInfo;

import java.util.Optional;

/** Gateway: consulta del caballo en el microservicio registro-caballo. */
public interface CaballoGateway {

    /**
     * Busca un caballo activo del usuario autenticado.
     *
     * @return vacío si no existe o no le pertenece
     * @throws com.equinos.dieta.domain.exception.ServicioNoDisponibleException si registro-caballo no responde
     */
    Optional<CaballoInfo> buscar(Long caballoId);
}
