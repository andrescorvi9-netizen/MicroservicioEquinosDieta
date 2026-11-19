package com.equinos.dieta.application.config;

import com.equinos.dieta.domain.model.gateway.CaballoGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoFuenteGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;
import com.equinos.dieta.domain.model.gateway.ClimaGateway;
import com.equinos.dieta.domain.model.gateway.DietaGateway;
import com.equinos.dieta.domain.model.gateway.NotificacionesGateway;
import com.equinos.dieta.domain.usecase.CatalogoUseCase;
import com.equinos.dieta.domain.usecase.DietaUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Crea los casos de uso y les entrega sus gateways. */
@Configuration
public class DietaConfig {

    @Bean
    public CatalogoUseCase catalogoUseCase(CatalogoGateway catalogoGateway, CatalogoFuenteGateway catalogoFuenteGateway) {
        return new CatalogoUseCase(catalogoGateway, catalogoFuenteGateway);
    }

    @Bean
    public DietaUseCase dietaUseCase(DietaGateway dietaGateway, CatalogoGateway catalogoGateway,
            CaballoGateway caballoGateway, ClimaGateway climaGateway, NotificacionesGateway notificacionesGateway) {
        return new DietaUseCase(dietaGateway, catalogoGateway, caballoGateway, climaGateway, notificacionesGateway);
    }
}
