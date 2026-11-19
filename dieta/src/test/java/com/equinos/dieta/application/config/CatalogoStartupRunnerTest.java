package com.equinos.dieta.application.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.usecase.CatalogoUseCase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

/** Pruebas de CatalogoStartupRunner. El caso de uso y los argumentos son simulados. */
@ExtendWith(MockitoExtension.class)
class CatalogoStartupRunnerTest {

    @Mock
    private CatalogoUseCase catalogoUseCase;
    @Mock
    private ApplicationArguments argumentos;

    @InjectMocks
    private CatalogoStartupRunner runner;

    @Test
    void alArrancarCargaElCatalogoSiEstaVacio() {
        //Arrange
        when(catalogoUseCase.cargarSiVacio()).thenReturn(15);

        //Act
        runner.run(argumentos);

        //Assert
        verify(catalogoUseCase).cargarSiVacio();
    }

    @Test
    void siElCatalogoYaEstabaCargadoNoFalla() {
        //Arrange
        when(catalogoUseCase.cargarSiVacio()).thenReturn(0);

        //Act
        runner.run(argumentos);

        //Assert
        verify(catalogoUseCase).cargarSiVacio();
    }
}
