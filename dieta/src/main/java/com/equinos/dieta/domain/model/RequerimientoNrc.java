package com.equinos.dieta.domain.model;

import java.math.BigDecimal;

/**
 * Requerimiento NRC de referencia de un perfil. Corresponde al peso de ejemplo del catálogo,
 * no al peso real del caballo: se muestra como referencia y no se escala.
 */
public record RequerimientoNrc(BigDecimal pesoReferenciaKg, BigDecimal energiaDigestibleMcalDia,
                               BigDecimal proteinaCrudaGDia, BigDecimal calcioGDia, BigDecimal fosforoGDia) {
}
