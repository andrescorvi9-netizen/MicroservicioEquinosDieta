package com.equinos.login.infraestructure.security;

import com.equinos.login.domain.model.gateway.EncrypterGateway;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Implementación del gateway: hash de contraseñas con BCrypt. */
@Component
public class EncrypterGatewayImpl implements EncrypterGateway {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String hash) {
        return encoder.matches(passwordPlano, hash);
    }
}
