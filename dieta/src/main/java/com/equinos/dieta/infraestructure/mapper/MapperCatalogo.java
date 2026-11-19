package com.equinos.dieta.infraestructure.mapper;

import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Porcentajes;
import com.equinos.dieta.domain.model.RequerimientoNrc;
import com.equinos.dieta.domain.model.TipoItem;
import com.equinos.dieta.infraestructure.driver_adapters.jpa_repository.PerfilDietaData;
import com.equinos.dieta.infraestructure.driver_adapters.jpa_repository.PerfilDietaItemData;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Convierte entre los perfiles del dominio y sus entidades JPA. */
@Component
public class MapperCatalogo {

    public PerfilDieta toPerfilDieta(PerfilDietaData entidad, String nombreEstado) {
        Map<TipoItem, List<String>> items = new EnumMap<>(TipoItem.class);
        for (PerfilDietaItemData item : entidad.getItems()) {
            items.computeIfAbsent(TipoItem.desde(item.getTipo()), tipo -> new ArrayList<>()).add(item.getTexto());
        }
        return new PerfilDieta(entidad.getId(), entidad.getCategoriaCodigo(), entidad.getEstadoCodigo(), nombreEstado,
                entidad.getFuente(), entidad.getCriterioNutricional(),
                new RequerimientoNrc(entidad.getPesoRefKg(), entidad.getEnergiaDigestibleMcalDia(), entidad.getProteinaCrudaGDia(),
                        entidad.getCalcioGDia(), entidad.getFosforoGDia()),
                new Porcentajes(entidad.getPctMsPeso(), entidad.getPctForraje(), entidad.getPctConcentrado()),
                entidad.getManejoAgua(), items);
    }

    public PerfilDietaData toData(PerfilDieta perfil) {
        PerfilDietaData entidad = new PerfilDietaData();
        entidad.setCategoriaCodigo(perfil.categoria());
        entidad.setEstadoCodigo(perfil.estadoSalud());
        entidad.setFuente(perfil.fuente());
        entidad.setCriterioNutricional(perfil.criterioNutricional());
        entidad.setPesoRefKg(perfil.requerimiento().pesoReferenciaKg());
        entidad.setEnergiaDigestibleMcalDia(perfil.requerimiento().energiaDigestibleMcalDia());
        entidad.setProteinaCrudaGDia(perfil.requerimiento().proteinaCrudaGDia());
        entidad.setCalcioGDia(perfil.requerimiento().calcioGDia());
        entidad.setFosforoGDia(perfil.requerimiento().fosforoGDia());
        entidad.setPctMsPeso(perfil.porcentajes().msDelPeso());
        entidad.setPctForraje(perfil.porcentajes().forraje());
        entidad.setPctConcentrado(perfil.porcentajes().concentrado());
        entidad.setManejoAgua(perfil.manejoAgua());
        for (TipoItem tipo : TipoItem.values()) {
            List<String> textos = perfil.items(tipo);
            for (int i = 0; i < textos.size(); i++) {
                entidad.getItems().add(new PerfilDietaItemData(entidad, tipo.codigo(), i + 1, textos.get(i)));
            }
        }
        return entidad;
    }
}
