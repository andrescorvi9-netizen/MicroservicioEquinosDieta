package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.TipoItem;

import java.math.BigDecimal;
import java.util.List;

/** Perfil del catálogo NRC tal como se entrega en la API. */
public record PerfilResponse(Long id, String categoria, String estadoSalud, String nombreEstado, String fuente,
                             boolean inferido, String criterioNutricional, Requerimiento requerimientoNrcReferencia,
                             BigDecimal porcentajeMateriaSecaDelPeso, BigDecimal porcentajeForraje,
                             BigDecimal porcentajeConcentrado, List<String> forrajeBase, List<String> forrajeEvitar,
                             List<String> concentradoRecomendado, List<String> concentradoEvitar, String manejoAgua,
                             List<String> suplementos, List<String> reglasAlimentacion, List<String> advertencias) {

    public record Requerimiento(BigDecimal pesoReferenciaKg, BigDecimal energiaDigestibleMcalDia,
                                BigDecimal proteinaCrudaGDia, BigDecimal calcioGDia, BigDecimal fosforoGDia) {
    }

    public static PerfilResponse de(PerfilDieta perfil) {
        return new PerfilResponse(perfil.id(), perfil.categoria(), perfil.estadoSalud(), perfil.nombreEstado(),
                perfil.fuente(), perfil.esInferido(), perfil.criterioNutricional(),
                new Requerimiento(perfil.requerimiento().pesoReferenciaKg(),
                        perfil.requerimiento().energiaDigestibleMcalDia(),
                        perfil.requerimiento().proteinaCrudaGDia(), perfil.requerimiento().calcioGDia(),
                        perfil.requerimiento().fosforoGDia()),
                perfil.porcentajes().msDelPeso(), perfil.porcentajes().forraje(), perfil.porcentajes().concentrado(),
                perfil.items(TipoItem.FORRAJE_BASE), perfil.items(TipoItem.FORRAJE_EVITAR),
                perfil.items(TipoItem.CONCENTRADO_RECOMENDADO), perfil.items(TipoItem.CONCENTRADO_EVITAR),
                perfil.manejoAgua(), perfil.items(TipoItem.SUPLEMENTO), perfil.items(TipoItem.REGLA_ALIMENTACION),
                perfil.items(TipoItem.ADVERTENCIA));
    }
}
