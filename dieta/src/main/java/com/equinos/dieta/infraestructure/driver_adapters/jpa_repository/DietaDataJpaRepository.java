package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietaDataJpaRepository extends JpaRepository<DietaData, Long> {

    Optional<DietaData> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    Page<DietaData> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<DietaData> findByUsuarioIdAndCaballoId(Long usuarioId, Long caballoId, Pageable pageable);

    /** La dieta más reciente del caballo: la base trae una sola fila. */
    Optional<DietaData> findFirstByUsuarioIdAndCaballoIdOrderByCreadoEnDescIdDesc(Long usuarioId, Long caballoId);

    long deleteByUsuarioId(Long usuarioId);
}
