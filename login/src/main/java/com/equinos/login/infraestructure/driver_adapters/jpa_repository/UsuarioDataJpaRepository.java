package com.equinos.login.infraestructure.driver_adapters.jpa_repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioDataJpaRepository extends JpaRepository<UsuarioData, Long> {

    Optional<UsuarioData> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
