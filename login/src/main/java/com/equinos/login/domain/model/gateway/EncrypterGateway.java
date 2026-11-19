package com.equinos.login.domain.model.gateway;


public interface EncrypterGateway {

    String hash(String passwordPlano);

    boolean coincide(String passwordPlano, String hash);
}
