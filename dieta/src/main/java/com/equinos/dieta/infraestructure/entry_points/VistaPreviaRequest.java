package com.equinos.dieta.infraestructure.entry_points;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VistaPreviaRequest(
        @NotBlank(message = "es obligatoria") String categoria,
        @NotBlank(message = "es obligatorio") String estadoSalud,
        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "1.0", message = "debe ser al menos 1 kg")
        @DecimalMax(value = "2000.0", message = "no puede superar 2000 kg")
        @Digits(integer = 4, fraction = 2, message = "admite máximo 2 decimales")
        BigDecimal pesoKg) {
}
