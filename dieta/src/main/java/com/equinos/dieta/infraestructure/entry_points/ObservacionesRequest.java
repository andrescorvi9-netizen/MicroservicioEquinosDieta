package com.equinos.dieta.infraestructure.entry_points;

import jakarta.validation.constraints.Size;

public record ObservacionesRequest(
        @Size(max = 2000, message = "admite máximo 2000 caracteres") String observaciones) {
}
