package com.equinos.registrocaballo.infraestructure.entry_points;

import jakarta.validation.constraints.NotBlank;

public record EstadoSaludRequest(@NotBlank(message = "es obligatorio") String estadoSalud) {
}
