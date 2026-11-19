package com.equinos.dieta.domain.usecase;

import com.equinos.dieta.domain.exception.RecursoNoEncontradoException;
import com.equinos.dieta.domain.exception.ReglaNegocioException;
import com.equinos.dieta.domain.model.CaballoInfo;
import com.equinos.dieta.domain.model.ClimaDia;
import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.DietaDetallada;
import com.equinos.dieta.domain.model.Pagina;
import com.equinos.dieta.domain.model.PerfilDieta;
import com.equinos.dieta.domain.model.Racion;
import com.equinos.dieta.domain.model.VistaPrevia;
import com.equinos.dieta.domain.model.gateway.CaballoGateway;
import com.equinos.dieta.domain.model.gateway.CatalogoGateway;
import com.equinos.dieta.domain.model.gateway.ClimaGateway;
import com.equinos.dieta.domain.model.gateway.DietaGateway;
import com.equinos.dieta.domain.model.gateway.NotificacionesGateway;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
public class DietaUseCase {

    private static final String NO_ENCONTRADA = "Dieta no encontrada";
    private static final int TAMANO_MAXIMO = 100;
    private static final int MAX_OBSERVACIONES = 2000;

    private final DietaGateway dietas;
    private final CatalogoGateway catalogo;
    private final CaballoGateway caballos;
    private final ClimaGateway clima;
    private final NotificacionesGateway notificaciones;

    public DietaDetallada generar(Long usuarioId, Long caballoId, String observaciones) {
        CaballoInfo caballo = caballos.buscar(caballoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Caballo no encontrado"));
        PerfilDieta perfil = perfilDe(caballo.categoria(), caballo.estadoSalud());
        Racion racion = Racion.calcular(caballo.pesoKg(), perfil.porcentajes());
        Dieta guardada = dietas.guardar(new Dieta(null, usuarioId, caballo.id(), caballo.nombre(),
                caballo.codigo(), perfil.id(), perfil.categoria(), perfil.estadoSalud(), caballo.pesoKg(), racion,
                limpiar(observaciones), Instant.now()));
        notificaciones.dietaGenerada(guardada);
        return new DietaDetallada(guardada, perfil, clima.climaDelDia().map(ClimaDia::aviso).orElse(null));
    }

    public DietaDetallada obtener(Long id, Long usuarioId) {
        return detallar(propia(id, usuarioId));
    }

    public Pagina<DietaDetallada> listar(Long usuarioId, int pagina, int tamano) {
        validarPaginacion(pagina, tamano);
        return dietas.listar(usuarioId, pagina, tamano).convertir(this::detallar);
    }

    @Transactional
    public DietaDetallada actualizarObservaciones(Long id, Long usuarioId, String observaciones) {
        Dieta actualizada = dietas.guardar(propia(id, usuarioId).conObservaciones(limpiar(observaciones)));
        return detallar(actualizada);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        if (!dietas.existe(id, usuarioId)) {
            throw new RecursoNoEncontradoException(NO_ENCONTRADA);
        }
        dietas.eliminar(id);
    }

    public VistaPrevia vistaPrevia(String categoria, String estadoSalud, BigDecimal pesoKg) {
        PerfilDieta perfil = perfilDe(categoria, estadoSalud);
        return new VistaPrevia(perfil.categoria(), perfil.estadoSalud(), pesoKg,
                Racion.calcular(pesoKg, perfil.porcentajes()), perfil);
    }

    public DietaDetallada regenerar(Long id, Long usuarioId) {
        Dieta anterior = propia(id, usuarioId);
        return generar(usuarioId, anterior.caballoId(), anterior.observaciones());
    }

    public Pagina<DietaDetallada> historial(Long usuarioId, Long caballoId, int pagina, int tamano) {
        validarPaginacion(pagina, tamano);
        return dietas.listarPorCaballo(usuarioId, caballoId, pagina, tamano).convertir(this::detallar);
    }

    public DietaDetallada vigente(Long usuarioId, Long caballoId) {
        return dietas.buscarVigente(usuarioId, caballoId).map(this::detallar)
                .orElseThrow(() -> new RecursoNoEncontradoException("El caballo no tiene dietas generadas"));
    }

    @Transactional
    public long eliminarTodasDeUsuario(Long usuarioId) {
        return dietas.eliminarPorUsuario(usuarioId);
    }

    private static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("Paginación no válida: page >= 0 y size entre 1 y " + TAMANO_MAXIMO);
        }
    }

    /** Una dieta ajena se trata igual que una inexistente. */
    private Dieta propia(Long id, Long usuarioId) {
        return dietas.buscar(id, usuarioId).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
    }

    /** Une la dieta con su perfil: cada llamada consulta el catálogo en la base de datos. */
    private DietaDetallada detallar(Dieta dieta) {
        PerfilDieta perfil = catalogo.buscarPerfilPorId(dieta.perfilId())
                .orElseThrow(() -> new IllegalStateException("Perfil " + dieta.perfilId() + " ausente del catálogo"));
        return new DietaDetallada(dieta, perfil);
    }

    private PerfilDieta perfilDe(String categoria, String estadoSalud) {
        String cat = CatalogoUseCase.normalizar(categoria);
        String est = CatalogoUseCase.normalizar(estadoSalud);
        return catalogo.buscarPerfil(cat, est).orElseThrow(() -> new ReglaNegocioException(
                "No existe un perfil de dieta para la categoría '" + cat + "' y el estado de salud '" + est + "'"));
    }

    private static String limpiar(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        String texto = observaciones.trim();
        if (texto.length() > MAX_OBSERVACIONES) {
            throw new ReglaNegocioException("Las observaciones admiten máximo " + MAX_OBSERVACIONES + " caracteres");
        }
        return texto;
    }
}
