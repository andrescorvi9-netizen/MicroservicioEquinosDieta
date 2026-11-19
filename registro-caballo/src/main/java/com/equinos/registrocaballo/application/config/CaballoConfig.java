package com.equinos.registrocaballo.application.config;

import com.equinos.registrocaballo.domain.model.gateway.CaballoGateway;
import com.equinos.registrocaballo.domain.model.gateway.NotificacionesGateway;
import com.equinos.registrocaballo.domain.usecase.CaballoUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Crea el caso de uso y le entrega su gateway. */
@Configuration
public class CaballoConfig {

    @Bean
    public CaballoUseCase caballoUseCase(CaballoGateway caballoGateway,
            NotificacionesGateway notificacionesGateway) {
        return new CaballoUseCase(caballoGateway, notificacionesGateway);
    }
}
