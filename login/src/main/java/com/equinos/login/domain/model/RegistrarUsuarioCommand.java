package com.equinos.login.domain.model;


public record RegistrarUsuarioCommand(String nombre, String correo, String password) {
}
