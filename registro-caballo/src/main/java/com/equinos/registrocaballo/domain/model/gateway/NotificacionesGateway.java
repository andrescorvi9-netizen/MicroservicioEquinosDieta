package com.equinos.registrocaballo.domain.model.gateway;

import com.equinos.registrocaballo.domain.model.Caballo;

/** Avisos al usuario. Nunca interrumpen la operación que los origina. */
public interface NotificacionesGateway {

    void estadoSaludCambiado(Caballo caballo);
}
