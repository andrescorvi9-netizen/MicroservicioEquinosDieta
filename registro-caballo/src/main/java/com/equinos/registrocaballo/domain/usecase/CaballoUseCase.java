package com.equinos.registrocaballo.domain.usecase;

import com.equinos.registrocaballo.domain.exception.ConflictoException;
import com.equinos.registrocaballo.domain.exception.RecursoNoEncontradoException;
import com.equinos.registrocaballo.domain.exception.ReglaNegocioException;
import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.DatosCaballo;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;
import com.equinos.registrocaballo.domain.model.Propietario;
import com.equinos.registrocaballo.domain.model.gateway.CaballoGateway;
import com.equinos.registrocaballo.domain.model.gateway.NotificacionesGateway;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de los caballos. Toda consulta se limita a los caballos del usuario autenticado. */
@RequiredArgsConstructor
public class CaballoUseCase {

    private static final String NO_ENCONTRADO = "Caballo no encontrado";
    private static final String CODIGO_EN_USO = "Ya existe un caballo activo con el código ";
    private static final int TAMANO_MAXIMO = 100;

    private final CaballoGateway caballos;
    private final NotificacionesGateway notificaciones;

    @Transactional
    public Caballo registrar(Propietario propietario, DatosCaballo datos) {
        String codigo = Caballo.normalizarCodigo(datos.codigo());
        if (caballos.existeCodigoActivo(codigo)) {
            throw new ConflictoException(CODIGO_EN_USO + codigo);
        }
        return caballos.guardar(Caballo.nuevo(propietario.id(), datos.nombre(), codigo, datos.pesoKg(),
                Categoria.desde(datos.categoria()), EstadoSalud.desde(datos.estadoSalud()), Instant.now()));
    }

    @Transactional(readOnly = true)
    public Caballo obtener(Long id, Long usuarioId) {
        return activoPropio(id, usuarioId);
    }

    @Transactional(readOnly = true)
    public Pagina<Caballo> listar(Long usuarioId, int pagina, int tamano) {
        validarPaginacion(pagina, tamano);
        return caballos.listarActivos(usuarioId, pagina, tamano);
    }

    @Transactional
    public Caballo actualizar(Long id, Long usuarioId, DatosCaballo datos) {
        Caballo caballo = activoPropio(id, usuarioId);
        String nuevoCodigo = Caballo.normalizarCodigo(datos.codigo());
        if (!nuevoCodigo.equals(caballo.getCodigo()) && caballos.existeCodigoActivo(nuevoCodigo)) {
            throw new ConflictoException(CODIGO_EN_USO + nuevoCodigo);
        }
        caballo.actualizar(datos.nombre(), nuevoCodigo, datos.pesoKg(), Categoria.desde(datos.categoria()),
                EstadoSalud.desde(datos.estadoSalud()), Instant.now());
        return caballos.guardar(caballo);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Caballo caballo = activoPropio(id, usuarioId);
        caballo.desactivar(Instant.now());
        caballos.guardar(caballo);
    }

    @Transactional
    public Caballo reactivar(Long id, Long usuarioId) {
        Caballo caballo = caballos.buscarCualquiera(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADO));
        if (caballo.isActivo()) {
            throw new ConflictoException("El caballo ya está activo");
        }
        if (caballos.existeCodigoActivo(caballo.getCodigo())) {
            throw new ConflictoException(CODIGO_EN_USO + caballo.getCodigo() + "; no se puede reactivar");
        }
        caballo.reactivar(Instant.now());
        return caballos.guardar(caballo);
    }

    @Transactional(readOnly = true)
    public Caballo buscarPorCodigo(String codigo, Long usuarioId) {
        return caballos.buscarActivoPorCodigo(Caballo.normalizarCodigo(codigo), usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADO));
    }

    @Transactional
    public Caballo actualizarPeso(Long id, Long usuarioId, BigDecimal pesoKg) {
        Caballo caballo = activoPropio(id, usuarioId);
        caballo.cambiarPeso(pesoKg, Instant.now());
        return caballos.guardar(caballo);
    }

    @Transactional
    public Caballo actualizarEstadoSalud(Long id, Long usuarioId, String estadoSalud) {
        Caballo caballo = activoPropio(id, usuarioId);
        EstadoSalud nuevoEstado = EstadoSalud.desde(estadoSalud);
        boolean cambio = caballo.getEstadoSalud() != nuevoEstado;
        caballo.cambiarEstadoSalud(nuevoEstado, Instant.now());
        Caballo guardado = caballos.guardar(caballo);
        if (cambio) {
            notificaciones.estadoSaludCambiado(caballo);
        }
        return guardado;
    }

    /** La búsqueda filtra y pagina en la base de datos, no en memoria. */
    @Transactional(readOnly = true)
    public Pagina<Caballo> buscar(Long usuarioId, String categoria, String estadoSalud, int pagina, int tamano) {
        validarPaginacion(pagina, tamano);
        Categoria filtroCategoria = vacio(categoria) ? null : Categoria.desde(categoria);
        EstadoSalud filtroEstado = vacio(estadoSalud) ? null : EstadoSalud.desde(estadoSalud);
        return caballos.buscarActivos(usuarioId, filtroCategoria, filtroEstado, pagina, tamano);
    }

    @Transactional
    public int eliminarTodosDeUsuario(Long usuarioId) {
        return caballos.eliminarTodosDeUsuario(usuarioId);
    }

    private static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("Paginación no válida: page >= 0 y size entre 1 y " + TAMANO_MAXIMO);
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /** Un caballo ajeno o borrado se trata igual que uno inexistente. */
    private Caballo activoPropio(Long id, Long usuarioId) {
        return caballos.buscarActivo(id, usuarioId).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADO));
    }
}
