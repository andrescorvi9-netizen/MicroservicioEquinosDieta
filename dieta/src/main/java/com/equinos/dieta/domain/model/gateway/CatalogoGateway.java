package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.PerfilDieta;

import java.util.List;
import java.util.Optional;

/** Gateway: persistencia del catálogo NRC. */
public interface CatalogoGateway {

    boolean estaVacio();

    void guardar(CatalogoMaestro catalogo);

    Optional<PerfilDieta> buscarPerfil(String categoria, String estadoSalud);

    Optional<PerfilDieta> buscarPerfilPorId(Long id);

    List<PerfilDieta> listarPerfiles();
}
