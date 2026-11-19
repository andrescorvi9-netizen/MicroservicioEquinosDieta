package com.equinos.login.infraestructure.entry_points;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank(message = "es obligatoria")
        String passwordActual,
        @NotBlank(message = "es obligatoria") @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String passwordNueva) {
}
