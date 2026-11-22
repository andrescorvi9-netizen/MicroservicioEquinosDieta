package com.equinos.registrocaballo.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.equinos.registrocaballo.domain.usecase.CaballoUseCase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de InternalCaballoController, la ruta que usa login al eliminar una cuenta. */
@ExtendWith(MockitoExtension.class)
class InternalCaballoControllerTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private CaballoUseCase caballoUseCase;

    @InjectMocks
    private InternalCaballoController controller;

    @Test
    void eliminarPorUsuarioIndicaCuantosCaballosBorro() {
        //Arrange
        when(caballoUseCase.eliminarTodosDeUsuario(USUARIO_ID)).thenReturn(3);

        //Act
        InternalCaballoController.LimpiezaUsuarioResponse respuesta = controller.eliminarPorUsuario(USUARIO_ID);

        //Assert
        assertEquals(USUARIO_ID, respuesta.usuarioId());
        assertEquals(3, respuesta.caballosEliminados());
    }
}
