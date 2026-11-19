package com.equinos.login.domain.model.gateway;

import com.equinos.login.domain.model.Usuario;

/** Avisos al usuario. Nunca interrumpen la operación que los origina. */
public interface NotificacionesGateway {

    void bienvenida(Usuario usuario);

    void contrasenaCambiada(Usuario usuario);
}
