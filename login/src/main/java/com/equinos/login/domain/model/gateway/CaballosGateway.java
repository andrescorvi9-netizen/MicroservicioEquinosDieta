package com.equinos.login.domain.model.gateway;

/** Gateway: operaciones internas sobre el microservicio registro-caballo. */
public interface CaballosGateway {


    void desactivarCaballosDeUsuario(Long usuarioId);
}
