package com.equinos.dieta.infraestructure.driver_adapters.jpa_repository;

import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.Pagina;
import com.equinos.dieta.domain.model.Racion;
import com.equinos.dieta.domain.model.gateway.DietaGateway;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Implementa DietaGateway: guarda y consulta dietas con Spring Data JPA. */
@Repository
@RequiredArgsConstructor
public class DietaDataGatewayImpl implements DietaGateway {

    private final DietaDataJpaRepository repositorio;

    @Override
    public Dieta guardar(Dieta dieta) {
        return aDominio(repositorio.save(aEntidad(dieta)));
    }

    @Override
    public Optional<Dieta> buscar(Long id, Long usuarioId) {
        return repositorio.findByIdAndUsuarioId(id, usuarioId).map(DietaDataGatewayImpl::aDominio);
    }

    @Override
    public boolean existe(Long id, Long usuarioId) {
        return repositorio.existsByIdAndUsuarioId(id, usuarioId);
    }

    @Override
    public Pagina<Dieta> listar(Long usuarioId, int pagina, int tamano) {
        return aPagina(repositorio.findByUsuarioId(usuarioId, recientesPrimero(pagina, tamano)));
    }

    @Override
    public Pagina<Dieta> listarPorCaballo(Long usuarioId, Long caballoId, int pagina, int tamano) {
        return aPagina(repositorio.findByUsuarioIdAndCaballoId(usuarioId, caballoId,
                recientesPrimero(pagina, tamano)));
    }

    @Override
    public Optional<Dieta> buscarVigente(Long usuarioId, Long caballoId) {
        return repositorio.findFirstByUsuarioIdAndCaballoIdOrderByCreadoEnDescIdDesc(usuarioId, caballoId)
                .map(DietaDataGatewayImpl::aDominio);
    }

    @Override
    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public long eliminarPorUsuario(Long usuarioId) {
        return repositorio.deleteByUsuarioId(usuarioId);
    }

    private static PageRequest recientesPrimero(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.desc("id")));
    }

    private static Pagina<Dieta> aPagina(Page<DietaData> resultado) {
        return new Pagina<>(resultado.getContent().stream().map(DietaDataGatewayImpl::aDominio).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
    }

    private static Dieta aDominio(DietaData e) {
        return new Dieta(e.getId(), e.getUsuarioId(), e.getCaballoId(), e.getCaballoNombre(), e.getCaballoCodigo(), e.getPerfilId(),
                e.getCategoria(), e.getEstadoSalud(), e.getPesoKg(), new Racion(e.getMsTotalKg(), e.getForrajeKg(), e.getConcentradoKg()),
                e.getObservaciones(), e.getCreadoEn());
    }

    private static DietaData aEntidad(Dieta dieta) {
        DietaData e = new DietaData();
        e.setId(dieta.id());
        e.setUsuarioId(dieta.usuarioId());
        e.setCaballoId(dieta.caballoId());
        e.setCaballoNombre(dieta.caballoNombre());
        e.setCaballoCodigo(dieta.caballoCodigo());
        e.setPerfilId(dieta.perfilId());
        e.setCategoria(dieta.categoria());
        e.setEstadoSalud(dieta.estadoSalud());
        e.setPesoKg(dieta.pesoKg());
        e.setMsTotalKg(dieta.racion().msTotalKg());
        e.setForrajeKg(dieta.racion().forrajeKg());
        e.setConcentradoKg(dieta.racion().concentradoKg());
        e.setObservaciones(dieta.observaciones());
        e.setCreadoEn(dieta.creadoEn());
        return e;
    }
}
