package com.equinos.login.application.config;

import com.equinos.login.domain.model.gateway.CaballosGateway;
import com.equinos.login.domain.model.gateway.DietasGateway;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;
import com.equinos.login.domain.model.gateway.PasswordFiltradaGateway;
import com.equinos.login.domain.model.gateway.TokenGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;
import com.equinos.login.domain.usecase.AutenticacionUseCase;
import com.equinos.login.domain.usecase.UsuarioUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Crea los casos de uso y les entrega sus gateways. */
@Configuration
public class UsuarioConfig {

    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateway usuarioGateway, EncrypterGateway encrypterGateway,
            DietasGateway dietasGateway, CaballosGateway caballosGateway,
            PasswordFiltradaGateway passwordFiltradaGateway, NotificacionesGateway notificacionesGateway) {
        return new UsuarioUseCase(usuarioGateway, encrypterGateway, dietasGateway, caballosGateway,
                passwordFiltradaGateway, notificacionesGateway);
    }

    @Bean
    public AutenticacionUseCase autenticacionUseCase(UsuarioGateway usuarioGateway, EncrypterGateway encrypterGateway,
            TokenGateway tokenGateway) {
        return new AutenticacionUseCase(usuarioGateway, encrypterGateway, tokenGateway);
    }
}
