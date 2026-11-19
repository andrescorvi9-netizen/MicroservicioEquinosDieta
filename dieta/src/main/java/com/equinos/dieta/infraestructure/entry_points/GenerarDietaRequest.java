package com.equinos.dieta.infraestructure.entry_points;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GenerarDietaRequest(
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser un identificador válido")
        Long caballoId,
        @Size(max = 2000, message = "admite máximo 2000 caracteres")
        String observaciones) {
}
