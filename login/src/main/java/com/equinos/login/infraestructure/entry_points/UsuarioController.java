package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.model.RegistrarUsuarioCommand;
import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.usecase.UsuarioUseCase;
import com.equinos.login.infraestructure.security.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de la cuenta de usuario. No contiene reglas de negocio: delega en el caso de uso. */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;

    /** CRUD - Crear. Público. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        Usuario creado = usuarioUseCase.registrar(
                new RegistrarUsuarioCommand(request.nombre(), request.correo(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(creado));
    }

    /** CRUD - Leer. Solo la cuenta propia. */
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return UsuarioResponse.de(usuarioUseCase.obtener(id, actual.id()));
    }

    /** CRUD - Actualizar. Solo la cuenta propia. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable("id") Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return UsuarioResponse.de(usuarioUseCase.actualizar(id, actual.id(), request.nombre(), request.correo()));
    }

    /** CRUD - Eliminar. Solo la cuenta propia. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        usuarioUseCase.eliminar(id, actual.id());
        return ResponseEntity.noContent().build();
    }

    /** Negocio - Cambio de contraseña: exige la contraseña actual. */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable("id") Long id,
                                                @Valid @RequestBody CambioPasswordRequest request,
                                                @AuthenticationPrincipal UsuarioAutenticado actual) {
        usuarioUseCase.cambiarPassword(id, actual.id(), request.passwordActual(), request.passwordNueva());
        return ResponseEntity.noContent().build();
    }

    /** Consulta pública: indica si un correo está libre para registrarse. */
    @GetMapping("/disponibilidad")
    public DisponibilidadResponse disponibilidad(@RequestParam("correo") String correo) {
        return new DisponibilidadResponse(Usuario.normalizarCorreo(correo), usuarioUseCase.correoDisponible(correo));
    }
}
