package com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaballoDataJpaRepository extends JpaRepository<CaballoData, Long> {

    Optional<CaballoData> findByIdAndUsuarioIdAndActivoTrue(Long id, Long usuarioId);

    Optional<CaballoData> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<CaballoData> findByCodigoAndUsuarioIdAndActivoTrue(String codigo, Long usuarioId);

    boolean existsByCodigoAndActivoTrue(String codigo);

    Page<CaballoData> findByUsuarioIdAndActivoTrue(Long usuarioId, Pageable pageable);

    // Variantes de la búsqueda filtrada. El DataGatewayImpl elige según los filtros presentes.

    Page<CaballoData> findByUsuarioIdAndActivoTrueAndCategoria(Long usuarioId, String categoria,
                                                                 Pageable pageable);

    Page<CaballoData> findByUsuarioIdAndActivoTrueAndEstadoSalud(Long usuarioId, String estadoSalud,
                                                                   Pageable pageable);

    Page<CaballoData> findByUsuarioIdAndActivoTrueAndCategoriaAndEstadoSalud(Long usuarioId, String categoria,
                                                                               String estadoSalud, Pageable pageable);

    /** Baja lógica masiva: la usa la eliminación de la cuenta del usuario. */
    @Modifying
    @Query("update CaballoData c set c.activo = false, c.actualizadoEn = :ahora "
            + "where c.usuarioId = :usuarioId and c.activo = true")
    int desactivarActivosDeUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);
}
