package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.usecase.DietaUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rutas internas entre microservicios. No usan JWT: las protege InternalKeyFilter
 * con el encabezado X-Internal-Key.
 */
@RestController
@RequestMapping("/api/internal/dietas")
@RequiredArgsConstructor
public class InternalDietaController {

    private final DietaUseCase dietaUseCase;

    /** Borra todas las dietas del usuario. La llama login al eliminar la cuenta. */
    @DeleteMapping("/usuario/{usuarioId}")
    public LimpiezaUsuarioResponse eliminarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return new LimpiezaUsuarioResponse(usuarioId, dietaUseCase.eliminarTodasDeUsuario(usuarioId));
    }

    public record LimpiezaUsuarioResponse(Long usuarioId, long dietasEliminadas) {
    }
}
