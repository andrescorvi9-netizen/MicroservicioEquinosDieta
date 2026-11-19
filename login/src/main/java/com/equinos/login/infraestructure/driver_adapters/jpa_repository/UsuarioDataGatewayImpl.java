package com.equinos.login.infraestructure.driver_adapters.jpa_repository;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.domain.model.gateway.UsuarioGateway;
import com.equinos.login.infraestructure.mapper.MapperUsuario;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Implementa el gateway de persistencia con Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class UsuarioDataGatewayImpl implements UsuarioGateway {

    private final MapperUsuario mapperUsuario;
    private final UsuarioDataJpaRepository repositorio;

    @Override
    public Usuario guardar(Usuario usuario) {
        return mapperUsuario.toUsuario(repositorio.save(mapperUsuario.toData(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapperUsuario::toUsuario);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repositorio.findByCorreo(correo).map(mapperUsuario::toUsuario);
    }

    @Override
    public boolean existePorId(Long id) {
        return repositorio.existsById(id);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repositorio.existsByCorreo(correo);
    }

    @Override
    public void eliminarPorId(Long id) {
        repositorio.deleteById(id);
    }
}
