package com.equinos.login.infraestructure.entry_points;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "es obligatorio") String correo,
        @NotBlank(message = "es obligatoria") String password) {
}
