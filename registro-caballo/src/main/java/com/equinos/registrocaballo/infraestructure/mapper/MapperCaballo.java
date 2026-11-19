package com.equinos.registrocaballo.infraestructure.mapper;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.infraestructure.driver_adapters.jpa_repository.CaballoData;

import org.springframework.stereotype.Component;

/** Convierte entre el modelo de dominio y la entidad JPA. */
@Component
public class MapperCaballo {

    public Caballo toCaballo(CaballoData entidad) {
        return new Caballo(entidad.getId(), entidad.getUsuarioId(), entidad.getNombre(), entidad.getCodigo(), entidad.getPesoKg(),
                Categoria.desde(entidad.getCategoria()), EstadoSalud.desde(entidad.getEstadoSalud()), entidad.isActivo(),
                entidad.getCreadoEn(), entidad.getActualizadoEn());
    }

    public CaballoData toData(Caballo caballo) {
        CaballoData entidad = new CaballoData();
        entidad.setId(caballo.getId());
        entidad.setUsuarioId(caballo.getUsuarioId());
        entidad.setNombre(caballo.getNombre());
        entidad.setCodigo(caballo.getCodigo());
        entidad.setPesoKg(caballo.getPesoKg());
        entidad.setCategoria(caballo.getCategoria().codigo());
        entidad.setEstadoSalud(caballo.getEstadoSalud().codigo());
        entidad.setActivo(caballo.isActivo());
        entidad.setCreadoEn(caballo.getCreadoEn());
        entidad.setActualizadoEn(caballo.getActualizadoEn());
        return entidad;
    }
}
