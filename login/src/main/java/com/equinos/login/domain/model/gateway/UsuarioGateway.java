package com.equinos.login.domain.model.gateway;

import com.equinos.login.domain.model.Usuario;

import java.util.Optional;


public interface UsuarioGateway {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorId(Long id);

    boolean existePorCorreo(String correo);

    void eliminarPorId(Long id);
}
