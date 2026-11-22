package com.equinos.login.domain.model.gateway;

/** Gateway: operaciones internas sobre el microservicio registro-caballo. */
public interface CaballosGateway {

    /** Borra de registro-caballo todos los caballos del usuario. */
    void eliminarCaballosDeUsuario(Long usuarioId);
}
