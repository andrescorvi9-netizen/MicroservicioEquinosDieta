package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerfilDietaDataJpaRepository extends JpaRepository<PerfilDietaData, Long> {

    /** Trae los perfiles con sus items en una sola consulta. */
    @Query("select distinct p from PerfilDietaData p left join fetch p.items order by p.id")
    List<PerfilDietaData> findAllConItems();
}
