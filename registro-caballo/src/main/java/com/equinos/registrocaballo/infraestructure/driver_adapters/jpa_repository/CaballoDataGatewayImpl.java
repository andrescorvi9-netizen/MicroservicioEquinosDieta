package com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;
import com.equinos.registrocaballo.domain.model.gateway.CaballoGateway;
import com.equinos.registrocaballo.infraestructure.mapper.MapperCaballo;

import java.time.Instant;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Implementa el gateway de persistencia con Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class CaballoDataGatewayImpl implements CaballoGateway {

    private final MapperCaballo mapperCaballo;
    private final CaballoDataJpaRepository repositorio;

    @Override
    public Caballo guardar(Caballo caballo) {
        return mapperCaballo.toCaballo(repositorio.save(mapperCaballo.toData(caballo)));
    }

    @Override
    public Optional<Caballo> buscarActivo(Long id, Long usuarioId) {
        return repositorio.findByIdAndUsuarioIdAndActivoTrue(id, usuarioId).map(mapperCaballo::toCaballo);
    }

    @Override
    public Optional<Caballo> buscarCualquiera(Long id, Long usuarioId) {
        return repositorio.findByIdAndUsuarioId(id, usuarioId).map(mapperCaballo::toCaballo);
    }

    @Override
    public Optional<Caballo> buscarActivoPorCodigo(String codigo, Long usuarioId) {
        return repositorio.findByCodigoAndUsuarioIdAndActivoTrue(codigo, usuarioId).map(mapperCaballo::toCaballo);
    }

    @Override
    public boolean existeCodigoActivo(String codigo) {
        return repositorio.existsByCodigoAndActivoTrue(codigo);
    }

    @Override
    public Pagina<Caballo> listarActivos(Long usuarioId, int pagina, int tamano) {
        return aPagina(repositorio.findByUsuarioIdAndActivoTrue(usuarioId, porNombre(pagina, tamano)));
    }

    @Override
    public Pagina<Caballo> buscarActivos(Long usuarioId, Categoria categoria, EstadoSalud estadoSalud, int pagina,
                                         int tamano) {
        Pageable orden = porNombre(pagina, tamano);
        Page<CaballoData> resultado;
        if (categoria != null && estadoSalud != null) {
            resultado = repositorio.findByUsuarioIdAndActivoTrueAndCategoriaAndEstadoSalud(usuarioId,
                    categoria.codigo(), estadoSalud.codigo(), orden);
        } else if (categoria != null) {
            resultado = repositorio.findByUsuarioIdAndActivoTrueAndCategoria(usuarioId, categoria.codigo(), orden);
        } else if (estadoSalud != null) {
            resultado = repositorio.findByUsuarioIdAndActivoTrueAndEstadoSalud(usuarioId, estadoSalud.codigo(),
                    orden);
        } else {
            resultado = repositorio.findByUsuarioIdAndActivoTrue(usuarioId, orden);
        }
        return aPagina(resultado);
    }

    @Override
    public int eliminarTodosDeUsuario(Long usuarioId) {
        return repositorio.eliminarTodosDeUsuario(usuarioId);
    }

    private static Pageable porNombre(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano, Sort.by("nombre").ascending());
    }

    private Pagina<Caballo> aPagina(Page<CaballoData> resultado) {
        return new Pagina<>(resultado.getContent().stream().map(mapperCaballo::toCaballo).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
    }
}
