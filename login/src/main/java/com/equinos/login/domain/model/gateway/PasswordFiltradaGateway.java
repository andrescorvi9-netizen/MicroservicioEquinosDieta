package com.equinos.login.domain.model.gateway;

public interface PasswordFiltradaGateway {

    /** Indica si la contraseña aparece en filtraciones de datos conocidas. */
    boolean estaFiltrada(String password);
}
