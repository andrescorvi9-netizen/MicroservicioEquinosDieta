package com.equinos.dieta.domain.model.gateway;

import com.equinos.dieta.domain.model.CatalogoMaestro;

/** Gateway: lectura del archivo maestro de dietas. */
public interface CatalogoFuenteGateway {

    CatalogoMaestro leer();
}
