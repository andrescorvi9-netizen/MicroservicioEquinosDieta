package com.equinos.dieta.infraestructure.driver_adapters.json_file;

import com.equinos.dieta.domain.model.CatalogoMaestro;
import com.equinos.dieta.domain.model.CategoriaCatalogo;
import com.equinos.dieta.domain.model.EstadoSaludCatalogo;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Porcentajes;
import com.equinos.dieta.domain.model.RequerimientoNrc;
import com.equinos.dieta.domain.model.TipoItem;
import com.equinos.dieta.domain.model.gateway.CatalogoFuenteGateway;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Implementación del gateway: lee el archivo maestro dietas_equinos_maestro.json del classpath
 * y lo convierte al modelo de dominio. Es el único punto del sistema que conoce la estructura del archivo.
 * El JSON se lee a mapas y listas de Java (Jackson 3, paquete tools.jackson) para navegarlo
 * con tipos estándar.
 */
@Component
public class CatalogoFuenteGatewayImpl implements CatalogoFuenteGateway {

    /** Campo del JSON que alimenta cada lista del perfil. */
    private static final Map<TipoItem, String> CAMPOS = new EnumMap<>(Map.of(
            TipoItem.FORRAJE_BASE, "forraje_base",
            TipoItem.FORRAJE_EVITAR, "forraje_evitar",
            TipoItem.CONCENTRADO_RECOMENDADO, "concentrado_recomendado",
            TipoItem.CONCENTRADO_EVITAR, "concentrado_evitar",
            TipoItem.SUPLEMENTO, "suplementos",
            TipoItem.REGLA_ALIMENTACION, "reglas_alimentacion",
            TipoItem.ADVERTENCIA, "advertencias"));

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String recurso;

    public CatalogoFuenteGatewayImpl(
            @Value("${app.catalogo.recurso:catalogo/dietas_equinos_maestro.json}") String recurso) {
        this.recurso = recurso;
    }

    @Override
    public CatalogoMaestro leer() {
        Map<String, Object> raiz;
        try (InputStream entrada = new ClassPathResource(recurso).getInputStream()) {
            raiz = objeto(mapper.readValue(entrada, Map.class));
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("No se pudo leer el archivo maestro de dietas: " + recurso, ex);
        }

        List<CategoriaCatalogo> categorias = new ArrayList<>();
        Map<String, String> estados = new LinkedHashMap<>();
        List<PerfilDieta> perfiles = new ArrayList<>();

        for (Map.Entry<String, Object> porCategoria : objeto(raiz.get("categorias")).entrySet()) {
            String codigoCategoria = porCategoria.getKey();
            Map<String, Object> categoria = objeto(porCategoria.getValue());
            categorias.add(new CategoriaCatalogo(codigoCategoria, texto(categoria, "nombre"),
                    texto(categoria, "descripcion"), texto(categoria, "notas_nrc")));

            for (Map.Entry<String, Object> porEstado : objeto(categoria.get("dietas_por_estado_salud")).entrySet()) {
                String codigoEstado = porEstado.getKey();
                Map<String, Object> perfil = objeto(porEstado.getValue());
                estados.putIfAbsent(codigoEstado, texto(perfil, "nombre_estado"));
                perfiles.add(perfil(codigoCategoria, codigoEstado, perfil));
            }
        }

        if (perfiles.isEmpty()) {
            throw new IllegalStateException("El archivo maestro no contiene perfiles de dieta: " + recurso);
        }
        List<EstadoSaludCatalogo> listaEstados = estados.entrySet().stream()
                .map(e -> new EstadoSaludCatalogo(e.getKey(), e.getValue())).toList();
        return new CatalogoMaestro(categorias, listaEstados, perfiles);
    }

    private PerfilDieta perfil(String categoria, String estado, Map<String, Object> nodo) {
        Map<String, Object> requerimiento = objeto(nodo.get("requerimiento_nrc_referencia"));
        Map<String, Object> racion = objeto(nodo.get("plan_racion_estimado"));
        BigDecimal pesoEjemplo = decimal(requerimiento, "peso_actual_kg_ejemplo");

        Map<TipoItem, List<String>> items = new EnumMap<>(TipoItem.class);
        for (Map.Entry<TipoItem, String> campo : CAMPOS.entrySet()) {
            items.put(campo.getKey(), textos(nodo.get(campo.getValue())));
        }

        return new PerfilDieta(null, categoria, estado, texto(nodo, "nombre_estado"),
                texto(nodo, "fuente"), texto(nodo, "criterio_nutricional"),
                new RequerimientoNrc(pesoEjemplo, decimal(requerimiento, "ED_Mcal_dia"),
                        decimal(requerimiento, "PC_g_dia"), decimal(requerimiento, "Ca_g_dia"),
                        decimal(requerimiento, "P_g_dia")),
                Porcentajes.desdeEjemplo(pesoEjemplo, decimal(racion, "materia_seca_total_kg_dia"),
                        decimal(racion, "forraje_kg_ms_dia")),
                texto(nodo, "manejo_agua"), items);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(Object valor) {
        if (!(valor instanceof Map)) {
            throw new IllegalStateException("El archivo maestro no tiene la estructura esperada");
        }
        return (Map<String, Object>) valor;
    }

    private static String texto(Map<String, Object> nodo, String campo) {
        Object valor = nodo.get(campo);
        return valor == null ? "" : String.valueOf(valor);
    }

    private static List<String> textos(Object valor) {
        if (!(valor instanceof List<?> lista)) {
            return List.of();
        }
        return lista.stream().map(String::valueOf).toList();
    }

    private static BigDecimal decimal(Map<String, Object> nodo, String campo) {
        Object valor = nodo.get(campo);
        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException("El archivo maestro no trae el número '" + campo + "'");
        }
        return new BigDecimal(numero.toString());
    }
}
