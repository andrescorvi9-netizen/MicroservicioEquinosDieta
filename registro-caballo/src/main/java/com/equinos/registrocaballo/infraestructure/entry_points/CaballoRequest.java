package com.equinos.registrocaballo.infraestructure.entry_points;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para registrar o actualizar un caballo. */
public record CaballoRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 120, message = "admite máximo 120 caracteres")
        String nombre,
        @NotBlank(message = "es obligatorio") @Size(max = 40, message = "admite máximo 40 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "solo admite letras, números, guion y guion bajo")
        String codigo,
        @NotNull(message = "es obligatorio")
        @DecimalMin(value = "1.0", message = "debe ser al menos 1 kg")
        @DecimalMax(value = "2000.0", message = "no puede superar 2000 kg")
        @Digits(integer = 4, fraction = 2, message = "admite máximo 2 decimales")
        BigDecimal pesoKg,
        @NotBlank(message = "es obligatoria") String categoria,
        @NotBlank(message = "es obligatorio") String estadoSalud) {
}
