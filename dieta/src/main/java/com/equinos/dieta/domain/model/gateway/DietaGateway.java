package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.Dieta;
import com.equinos.dieta.domain.model.Pagina;

import java.util.Optional;

/** Gateway: persistencia de las dietas generadas. */
public interface DietaGateway {

    Dieta guardar(Dieta dieta);

    Optional<Dieta> buscar(Long id, Long usuarioId);

    boolean existe(Long id, Long usuarioId);

    Pagina<Dieta> listar(Long usuarioId, int pagina, int tamano);

    /** Historial de un caballo, de la más reciente a la más antigua. */
    Pagina<Dieta> listarPorCaballo(Long usuarioId, Long caballoId, int pagina, int tamano);

    /** La dieta más reciente del caballo, sin cargar el resto del historial. */
    Optional<Dieta> buscarVigente(Long usuarioId, Long caballoId);

    void eliminar(Long id);

    /** Borra todas las dietas del usuario. Devuelve cuántas borró. Se usa al eliminar la cuenta. */
    long eliminarPorUsuario(Long usuarioId);
}
