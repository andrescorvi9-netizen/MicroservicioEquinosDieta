package com.equinos.notificaciones.infraestructure.entry_points;

import com.equinos.notificaciones.domain.usecase.NotificacionUseCase;

import java.util.concurrent.Executor;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ruta interna: la llaman los demás microservicios con el encabezado X-Internal-Key. */
@RestController
@RequestMapping("/api/internal/notificaciones")
@RequiredArgsConstructor
public class InternalNotificacionController {

    private final NotificacionUseCase notificacionUseCase;
    private final Executor notificacionesExecutor;

    /** Responde 202 de inmediato: el correo se envía en segundo plano. */
    @PostMapping
    public ResponseEntity<Void> recibir(@Valid @RequestBody NotificacionRequest request) {
        notificacionesExecutor.execute(() -> notificacionUseCase.enviar(request.aDominio()));
        return ResponseEntity.accepted().build();
    }
}
