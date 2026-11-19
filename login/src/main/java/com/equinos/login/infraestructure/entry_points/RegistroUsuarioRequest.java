package com.equinos.login.infraestructure.entry_points;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 120, message = "admite máximo 120 caracteres")
        String nombre,
        @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido")
        @Size(max = 160, message = "admite máximo 160 caracteres")
        String correo,
        @NotBlank(message = "es obligatoria") @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password) {
}
