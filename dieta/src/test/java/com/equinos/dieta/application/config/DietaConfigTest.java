package com.equinos.dieta.application.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.equinos.dieta.domain.model.gateway.CaballoGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoFuenteGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;
import com.equinos.dieta.domain.model.gateway.DietaGateway;
import com.equinos.dieta.domain.model.gateway.ClimaGateway;
import com.equinos.dieta.domain.model.gateway.NotificacionesGateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de DietaConfig: crea los casos de uso con sus gateways simulados. */
@ExtendWith(MockitoExtension.class)
class DietaConfigTest {

    @Mock
    private DietaGateway dietas;
    @Mock
    private CatalogoGateway catalogo;
    @Mock
    private CatalogoFuenteGateway fuente;
    @Mock
    private CaballoGateway caballos;
    @Mock
    private ClimaGateway clima;
    @Mock
    private NotificacionesGateway notificaciones;

    private final DietaConfig config = new DietaConfig();

    @Test
    void creaElCasoDeUsoDeCatalogo() {
        //Act y Assert
        assertNotNull(config.catalogoUseCase(catalogo, fuente));
    }

    @Test
    void creaElCasoDeUsoDeDieta() {
        //Act y Assert
        assertNotNull(config.dietaUseCase(dietas, catalogo, caballos, clima, notificaciones));
    }
}
