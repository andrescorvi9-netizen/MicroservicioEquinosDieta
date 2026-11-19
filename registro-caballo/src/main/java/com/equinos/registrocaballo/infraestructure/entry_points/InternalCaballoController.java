package com.equinos.registrocaballo.infraestructure.entry_points;

import com.equinos.registrocaballo.domain.usecase.CaballoUseCase;

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
@RequestMapping("/api/internal/caballos")
@RequiredArgsConstructor
public class InternalCaballoController {

    private final CaballoUseCase caballoUseCase;

    /** Baja lógica de todos los caballos del usuario. La llama login al eliminar la cuenta. */
    @DeleteMapping("/usuario/{usuarioId}")
    public BajaUsuarioResponse desactivarPorUsuario(@PathVariable("usuarioId") Long usuarioId) {
        return new BajaUsuarioResponse(usuarioId, caballoUseCase.desactivarTodosDeUsuario(usuarioId));
    }

    public record BajaUsuarioResponse(Long usuarioId, int caballosDesactivados) {
    }
}
