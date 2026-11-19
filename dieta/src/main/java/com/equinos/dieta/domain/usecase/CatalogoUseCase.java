package com.equinos.dieta.domain.usecase;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.gateway.CatalogoFuenteGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;

import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso del catálogo NRC. */
@RequiredArgsConstructor
public class CatalogoUseCase {

    private final CatalogoGateway catalogo;
    private final CatalogoFuenteGateway fuente;

    public List<PerfilDieta> perfiles(String categoria, String estadoSalud) {
        String filtroCategoria = normalizar(categoria);
        String filtroEstado = normalizar(estadoSalud);
        return catalogo.listarPerfiles().stream()
                .filter(p -> filtroCategoria.isEmpty() || p.categoria().equals(filtroCategoria))
                .filter(p -> filtroEstado.isEmpty() || p.estadoSalud().equals(filtroEstado))
                .toList();
    }

    @Transactional
    public int cargarSiVacio() {
        if (!catalogo.estaVacio()) {
            return 0;
        }
        CatalogoMaestro maestro = fuente.leer();
        catalogo.guardar(maestro);
        return maestro.perfiles().size();
    }

    static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
