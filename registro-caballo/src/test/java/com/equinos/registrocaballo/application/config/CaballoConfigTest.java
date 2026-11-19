package com.equinos.registrocaballo.application.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.equinos.registrocaballo.domain.model.gateway.CaballoGateway;
import com.equinos.registrocaballo.domain.model.gateway.NotificacionesGateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba de CaballoConfig: crea el caso de uso con su gateway simulado. */
@ExtendWith(MockitoExtension.class)
class CaballoConfigTest {

    @Mock
    private CaballoGateway caballoGateway;
    @Mock
    private NotificacionesGateway notificacionesGateway;

    @Test
    void creaElCasoDeUsoDeCaballo() {
        //Act y Assert
        assertNotNull(new CaballoConfig().caballoUseCase(caballoGateway, notificacionesGateway));
    }
}
