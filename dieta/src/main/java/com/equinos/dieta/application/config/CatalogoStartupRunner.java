package com.equinos.dieta.application.config;

import com.equinos.dieta.domain.usecase.CatalogoUseCase;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar el servicio (después de que Flyway creó las tablas) carga el archivo maestro
 * si el catálogo está vacío. En los arranques siguientes no hace nada.
 */
@Component
@RequiredArgsConstructor
public class CatalogoStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoStartupRunner.class);

    private final CatalogoUseCase catalogoUseCase;

    @Override
    public void run(ApplicationArguments args) {
        int cargados = catalogoUseCase.cargarSiVacio();
        if (cargados > 0) {
            log.info("Catálogo NRC cargado desde el archivo maestro: {} perfiles", cargados);
        } else {
            log.info("El catálogo NRC ya estaba cargado; no se modifica");
        }
    }
}
