package com.equinos.registrocaballo.infraestructure.entry_points;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.DatosCaballo;
import com.equinos.registrocaballo.domain.model.Propietario;
import com.equinos.registrocaballo.domain.usecase.CaballoUseCase;
import com.equinos.registrocaballo.infraestructure.security.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de caballos. No contiene reglas de negocio: delega en los casos de uso. */
@RestController
@RequestMapping("/api/caballos")
@RequiredArgsConstructor
public class CaballoController {

    private final CaballoUseCase caballoUseCase;

    // ---------- CRUD ----------

    /** CRUD - Crear. Responde 409 si el código ya lo usa un caballo activo. */
    @PostMapping
    public ResponseEntity<CaballoResponse> registrar(@Valid @RequestBody CaballoRequest request,
                                                     @AuthenticationPrincipal UsuarioAutenticado actual) {
        Caballo creado = caballoUseCase.registrar(new Propietario(actual.id(), actual.nombre(), actual.correo()),
                datos(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CaballoResponse.de(creado));
    }

    /** CRUD - Leer uno. */
    @GetMapping("/{id}")
    public CaballoResponse obtener(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.obtener(id, actual.id()));
    }

    /** CRUD - Leer la lista paginada de caballos activos del usuario. */
    @GetMapping
    public PaginaResponse<CaballoResponse> listar(@RequestParam(name = "page", defaultValue = "0") int page,
                                                  @RequestParam(name = "size", defaultValue = "10") int size,
                                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return PaginaResponse.de(caballoUseCase.listar(actual.id(), page, size).convertir(CaballoResponse::de));
    }

    /** CRUD - Actualizar todos los datos. */
    @PutMapping("/{id}")
    public CaballoResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody CaballoRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.actualizar(id, actual.id(), datos(request)));
    }

    /** CRUD - Eliminar (borrado lógico: activo = false). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        caballoUseCase.eliminar(id, actual.id());
        return ResponseEntity.noContent().build();
    }

    // ---------- Endpoints de negocio ----------

    /** Negocio - Reactivar un caballo borrado. 409 si ya está activo o si su código lo tomó otro caballo. */
    @PostMapping("/{id}/reactivacion")
    public CaballoResponse reactivar(@PathVariable("id") Long id,
                                     @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.reactivar(id, actual.id()));
    }

    /** Negocio - Cambio de estado de salud: determina el perfil de dieta que aplicará al caballo. */
    @PatchMapping("/{id}/estado-salud")
    public CaballoResponse actualizarEstadoSalud(@PathVariable("id") Long id,
                                                 @Valid @RequestBody EstadoSaludRequest request,
                                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.actualizarEstadoSalud(id, actual.id(), request.estadoSalud()));
    }

    /** Actualización del peso: es la base del cálculo de la ración. */
    @PatchMapping("/{id}/peso")
    public CaballoResponse actualizarPeso(@PathVariable("id") Long id, @Valid @RequestBody PesoRequest request,
                                          @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.actualizarPeso(id, actual.id(), request.pesoKg()));
    }

    /** Consulta por código entre los caballos activos del usuario. */
    @GetMapping("/codigo/{codigo}")
    public CaballoResponse buscarPorCodigo(@PathVariable("codigo") String codigo,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return CaballoResponse.de(caballoUseCase.buscarPorCodigo(codigo, actual.id()));
    }

    /** Consulta paginada, filtrada por categoría y/o estado de salud. */
    @GetMapping("/busqueda")
    public PaginaResponse<CaballoResponse> buscar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal UsuarioAutenticado actual) {
        return PaginaResponse.de(
                caballoUseCase.buscar(actual.id(), categoria, estado, page, size).convertir(CaballoResponse::de));
    }

    private static DatosCaballo datos(CaballoRequest request) {
        return new DatosCaballo(request.nombre(), request.codigo(), request.pesoKg(), request.categoria(),
                request.estadoSalud());
    }
}
