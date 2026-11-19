package com.equinos.login.application.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.equinos.login.domain.model.gateway.CaballosGateway;
import com.equinos.login.domain.model.gateway.DietasGateway;
import com.equinos.login.domain.model.gateway.EncrypterGateway;
import com.equinos.login.domain.model.gateway.TokenGateway;
import com.equinos.login.domain.model.gateway.UsuarioGateway;
import com.equinos.login.domain.model.gateway.NotificacionesGateway;
import com.equinos.login.domain.model.gateway.PasswordFiltradaGateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de UsuarioConfig: crea los casos de uso con sus gateways simulados. */
@ExtendWith(MockitoExtension.class)
class UsuarioConfigTest {

    @Mock
    private UsuarioGateway usuarios;
    @Mock
    private EncrypterGateway hasher;
    @Mock
    private DietasGateway dietas;
    @Mock
    private CaballosGateway caballos;
    @Mock
    private TokenGateway tokens;
    @Mock
    private PasswordFiltradaGateway passwordsFiltradas;
    @Mock
    private NotificacionesGateway notificaciones;

    private final UsuarioConfig config = new UsuarioConfig();

    @Test
    void creaElCasoDeUsoDeUsuario() {
        //Act y Assert
        assertNotNull(config.usuarioUseCase(usuarios, hasher, dietas, caballos, passwordsFiltradas, notificaciones));
    }

    @Test
    void creaElCasoDeUsoDeAutenticacion() {
        //Act y Assert
        assertNotNull(config.autenticacionUseCase(usuarios, hasher, tokens));
    }
}
