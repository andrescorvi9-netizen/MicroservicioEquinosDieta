package com.equinos.login.infraestructure.entry_points;

import com.equinos.login.domain.usecase.AutenticacionUseCase;
import com.equinos.login.infraestructure.security.UsuarioAutenticado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de autenticación. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticacionUseCase autenticacionUseCase;

    /** Negocio - Inicio de sesión. Público. Controla intentos fallidos y bloqueo de 15 minutos. */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.de(autenticacionUseCase.iniciarSesion(request.correo(), request.password()));
    }

    /** Negocio - Renovación: entrega un token nuevo a partir de uno todavía vigente. */
    @PostMapping("/renovacion")
    public TokenResponse renovar(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return TokenResponse.de(autenticacionUseCase.renovar(actual.id()));
    }

    /** Datos actuales del usuario autenticado. */
    @GetMapping("/perfil")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return UsuarioResponse.de(autenticacionUseCase.perfil(actual.id()));
    }
}
