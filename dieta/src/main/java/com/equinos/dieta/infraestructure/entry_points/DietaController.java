package com.equinos.dieta.infraestructure.entry_points;

import com.equinos.dieta.domain.usecase.CatalogoUseCase;
import com.equinos.dieta.domain.usecase.DietaUseCase;
import com.equinos.dieta.infraestructure.security.UsuarioAutenticado;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de dietas. No contiene reglas de negocio: delega en los casos de uso. */
@RestController
@RequestMapping("/api/dietas")
@RequiredArgsConstructor
public class DietaController {

    private final DietaUseCase dietaUseCase;
    private final CatalogoUseCase catalogoUseCase;

    // ---------- CRUD ----------

    /** CRUD - Crear: genera la dieta de un caballo del usuario y la guarda. */
    @PostMapping
    public ResponseEntity<DietaResponse> generar(@Valid @RequestBody GenerarDietaRequest request,
                                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        DietaResponse respuesta = DietaResponse.de(
                dietaUseCase.generar(actual.id(), request.caballoId(), request.observaciones()));
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** CRUD - Leer una. */
    @GetMapping("/{id}")
    public DietaResponse obtener(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return DietaResponse.de(dietaUseCase.obtener(id, actual.id()));
    }

    /** CRUD - Leer la lista paginada de dietas del usuario, de la más reciente a la más antigua. */
    @GetMapping
    public PaginaResponse<DietaResponse> listar(@RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size,
                                                @AuthenticationPrincipal UsuarioAutenticado actual) {
        return PaginaResponse.de(dietaUseCase.listar(actual.id(), page, size).convertir(DietaResponse::de));
    }

    /** CRUD - Actualizar: solo las observaciones. */
    @PutMapping("/{id}")
    public DietaResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody ObservacionesRequest request,
                                    @AuthenticationPrincipal UsuarioAutenticado actual) {
        return DietaResponse.de(dietaUseCase.actualizarObservaciones(id, actual.id(), request.observaciones()));
    }

    /** CRUD - Eliminar. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        dietaUseCase.eliminar(id, actual.id());
        return ResponseEntity.noContent().build();
    }

    // ---------- Endpoints de negocio ----------

    /** Negocio - Vista previa: calcula con categoría, estado y peso, sin guardar. */
    @PostMapping("/vista-previa")
    public VistaPreviaResponse vistaPrevia(@Valid @RequestBody VistaPreviaRequest request) {
        return VistaPreviaResponse.de(
                dietaUseCase.vistaPrevia(request.categoria(), request.estadoSalud(), request.pesoKg()));
    }

    /** Negocio - Regeneración: crea una dieta nueva con los datos actuales del caballo. */
    @PostMapping("/{id}/regeneracion")
    public ResponseEntity<DietaResponse> regenerar(@PathVariable("id") Long id,
                                                   @AuthenticationPrincipal UsuarioAutenticado actual) {
        return ResponseEntity.status(HttpStatus.CREATED).body(DietaResponse.de(dietaUseCase.regenerar(id, actual.id())));
    }

    /** Negocio - Dieta vigente: la más reciente del caballo. */
    @GetMapping("/caballo/{caballoId}/vigente")
    public DietaResponse vigente(@PathVariable("caballoId") Long caballoId,
                                 @AuthenticationPrincipal UsuarioAutenticado actual) {
        return DietaResponse.de(dietaUseCase.vigente(actual.id(), caballoId));
    }

    /** Historial de dietas de un caballo, paginado, de la más reciente a la más antigua. */
    @GetMapping("/caballo/{caballoId}")
    public PaginaResponse<DietaResponse> historial(@PathVariable("caballoId") Long caballoId,
                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                                   @AuthenticationPrincipal UsuarioAutenticado actual) {
        return PaginaResponse.de(
                dietaUseCase.historial(actual.id(), caballoId, page, size).convertir(DietaResponse::de));
    }

    /** Consulta del catálogo NRC, con filtros opcionales por categoría y estado de salud. */
    @GetMapping("/perfiles")
    public List<PerfilResponse> perfiles(@RequestParam(name = "categoria", required = false) String categoria,
                                         @RequestParam(name = "estado", required = false) String estado) {
        return catalogoUseCase.perfiles(categoria, estado).stream().map(PerfilResponse::de).toList();
    }
}
