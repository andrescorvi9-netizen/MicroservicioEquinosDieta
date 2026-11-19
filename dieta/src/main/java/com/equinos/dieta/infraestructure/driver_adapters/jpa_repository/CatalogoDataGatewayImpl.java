package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;
import com.equinos.dieta.infraestructure.mapper.MapperCatalogo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del gateway: persistencia del catálogo NRC.
 * Cada consulta lee los perfiles de la base (con sus items, en una consulta con join fetch).
 */
@Repository
@RequiredArgsConstructor
public class CatalogoDataGatewayImpl implements CatalogoGateway {

    private final MapperCatalogo mapperCatalogo;
    private final CategoriaDataJpaRepository categorias;
    private final EstadoSaludDataJpaRepository estados;
    private final PerfilDietaDataJpaRepository perfiles;

    @Override
    public boolean estaVacio() {
        return perfiles.count() == 0;
    }

    @Override
    @Transactional
    public void guardar(CatalogoMaestro catalogo) {
        categorias.saveAll(catalogo.categorias().stream()
                .map(c -> new CategoriaData(c.codigo(), c.nombre(), c.descripcion(), c.notasNrc())).toList());
        estados.saveAll(catalogo.estados().stream()
                .map(e -> new EstadoSaludData(e.codigo(), e.nombre())).toList());
        // flush: las categorías y los estados deben existir antes de insertar los perfiles (claves foráneas)
        categorias.flush();
        estados.flush();
        perfiles.saveAll(catalogo.perfiles().stream().map(mapperCatalogo::toData).toList());
    }

    @Override
    public Optional<PerfilDieta> buscarPerfil(String categoria, String estadoSalud) {
        return leerDeBase().stream()
                .filter(p -> p.categoria().equals(categoria) && p.estadoSalud().equals(estadoSalud))
                .findFirst();
    }

    @Override
    public Optional<PerfilDieta> buscarPerfilPorId(Long id) {
        return leerDeBase().stream().filter(p -> p.id().equals(id)).findFirst();
    }

    @Override
    public List<PerfilDieta> listarPerfiles() {
        return leerDeBase();
    }

    /**
     * Lee los 15 perfiles con sus items en una sola consulta (join fetch), de modo que no
     * se necesita una sesión de Hibernate abierta después de este método.
     */
    private List<PerfilDieta> leerDeBase() {
        Map<String, String> nombres = new HashMap<>();
        for (EstadoSaludData estado : estados.findAll()) {
            nombres.put(estado.getCodigo(), estado.getNombre());
        }
        return perfiles.findAllConItems().stream()
                .map(p -> mapperCatalogo.toPerfilDieta(p, nombres.get(p.getEstadoCodigo())))
                .toList();
    }
}
