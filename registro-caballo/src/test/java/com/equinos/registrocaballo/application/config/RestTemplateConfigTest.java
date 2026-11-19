package com.equinos.registrocaballo.application.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/** Prueba de RestTemplateConfig: entrega el cliente HTTP para llamar a otros microservicios. */
class RestTemplateConfigTest {

    @Test
    void restTemplateCreaElClienteHttp() {
        //Act
        RestTemplate restTemplate = new RestTemplateConfig().restTemplate();

        //Assert
        assertNotNull(restTemplate);
    }
}
