package com.equinos.login.infraestructure.mapper;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.infraestructure.driver_adapters.jpa_repository.UsuarioData;

import org.springframework.stereotype.Component;

/** Convierte entre el modelo de dominio y la entidad JPA. */
@Component
public class MapperUsuario {

    public Usuario toUsuario(UsuarioData entidad) {
        return new Usuario(entidad.getId(), entidad.getNombre(), entidad.getCorreo(), entidad.getPasswordHash(),
                entidad.getIntentosFallidos(), entidad.getBloqueadoHasta(), entidad.getCreadoEn(), entidad.getActualizadoEn());
    }

    public UsuarioData toData(Usuario usuario) {
        UsuarioData entidad = new UsuarioData();
        entidad.setId(usuario.getId());
        entidad.setNombre(usuario.getNombre());
        entidad.setCorreo(usuario.getCorreo());
        entidad.setPasswordHash(usuario.getPasswordHash());
        entidad.setIntentosFallidos(usuario.getIntentosFallidos());
        entidad.setBloqueadoHasta(usuario.getBloqueadoHasta());
        entidad.setCreadoEn(usuario.getCreadoEn());
        entidad.setActualizadoEn(usuario.getActualizadoEn());
        return entidad;
    }
}
