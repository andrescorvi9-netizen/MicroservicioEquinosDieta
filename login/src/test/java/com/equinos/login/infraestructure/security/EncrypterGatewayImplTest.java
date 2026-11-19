package com.equinos.login.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas de EncrypterGatewayImpl con BCrypt, que es justo lo que esta clase envuelve. */
class EncrypterGatewayImplTest {

    private final EncrypterGatewayImpl gateway = new EncrypterGatewayImpl();

    @Test
    void hashNoGuardaLaContrasenaEnTextoPlano() {
        //Act
        String hash = gateway.hash("Clave12345");

        //Assert
        assertNotEquals("Clave12345", hash);
    }

    @Test
    void coincideReconoceLaContrasenaCorrecta() {
        //Arrange
        String hash = gateway.hash("Clave12345");

        //Act y Assert
        assertTrue(gateway.coincide("Clave12345", hash));
        assertFalse(gateway.coincide("OtraClave1", hash));
    }
}
