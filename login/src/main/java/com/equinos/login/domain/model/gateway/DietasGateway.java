package com.equinos.login.domain.model.gateway;

/** Gateway: operaciones internas sobre el microservicio dieta. */
public interface DietasGateway {

    void eliminarDietasDeUsuario(Long usuarioId);
}
