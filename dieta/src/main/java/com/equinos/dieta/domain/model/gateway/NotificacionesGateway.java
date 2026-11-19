package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.Dieta;

/** Avisos al usuario. Nunca interrumpen la operación que los origina. */
public interface NotificacionesGateway {

    void dietaGenerada(Dieta dieta);
}
