package com.equinos.dieta.infraestructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.equinos.dieta.domain.usecase.DietaUseCase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de InternalDietaController, la ruta que usa login al eliminar una cuenta. */
@ExtendWith(MockitoExtension.class)
class InternalDietaControllerTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private DietaUseCase dietaUseCase;

    @InjectMocks
    private InternalDietaController controller;

    @Test
    void eliminarPorUsuarioIndicaCuantasDietasBorro() {
        //Arrange
        when(dietaUseCase.eliminarTodasDeUsuario(USUARIO_ID)).thenReturn(4L);

        //Act
        InternalDietaController.LimpiezaUsuarioResponse respuesta = controller.eliminarPorUsuario(USUARIO_ID);

        //Assert
        assertEquals(USUARIO_ID, respuesta.usuarioId());
        assertEquals(4L, respuesta.dietasEliminadas());
    }
}
