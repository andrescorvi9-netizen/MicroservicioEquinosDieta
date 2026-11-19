package com.equinos.registrocaballo.domain.model.gateway;

import com.equinos.registrocaballo.domain.model.Caballo;
import com.equinos.registrocaballo.domain.model.Categoria;
import com.equinos.registrocaballo.domain.model.EstadoSalud;
import com.equinos.registrocaballo.domain.model.Pagina;

import java.time.Instant;
import java.util.Optional;

/** Gateway: persistencia de caballos. */
public interface CaballoGateway {

    Caballo guardar(Caballo caballo);

    Optional<Caballo> buscarActivo(Long id, Long usuarioId);

    /** Incluye los caballos borrados; se usa para la reactivación. */
    Optional<Caballo> buscarCualquiera(Long id, Long usuarioId);

    Optional<Caballo> buscarActivoPorCodigo(String codigo, Long usuarioId);

    /** Verifica el código entre los caballos activos de todos los usuarios. */
    boolean existeCodigoActivo(String codigo);

    Pagina<Caballo> listarActivos(Long usuarioId, int pagina, int tamano);

    /** Búsqueda paginada con filtros opcionales: un filtro nulo no se aplica. */
    Pagina<Caballo> buscarActivos(Long usuarioId, Categoria categoria, EstadoSalud estadoSalud, int pagina,
                                  int tamano);

    /** Baja lógica de todos los caballos activos del usuario. Devuelve cuántos dio de baja. */
    int desactivarActivosDeUsuario(Long usuarioId, Instant ahora);
}
