package com.equinos.dieta.domain.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Hora más calurosa del día en la zona de referencia. El índice de estrés térmico es una regla práctica
 * (temperatura en °F + humedad relativa en %), no un valor del NRC.
 */
public record ClimaDia(String zona, double temperaturaC, int humedad) {

    private static final int PRECAUCION = 130;
    private static final int RIESGO = 150;
    private static final int PELIGRO = 180;

    /**
     * Usa la humedad de esa misma hora. No se toma el mayor índice del día porque las madrugadas frías y muy
     * húmedas darían un índice alto sin que exista calor.
     */
    public static Optional<ClimaDia> deLaHoraMasCalurosa(String zona, List<Double> temperaturas, List<Double> humedades) {
        ClimaDia peor = null;
        for (int i = 0; i < Math.min(temperaturas.size(), humedades.size()); i++) {
            if (temperaturas.get(i) != null && humedades.get(i) != null) {
                ClimaDia hora = new ClimaDia(zona, temperaturas.get(i), (int) Math.round(humedades.get(i)));
                peor = peor == null || hora.temperaturaC() > peor.temperaturaC() ? hora : peor;
            }
        }
        return Optional.ofNullable(peor);
    }

    public int indice() {
        return (int) Math.round(temperaturaC * 9 / 5 + 32) + humedad;
    }

    public String aviso() {
        return String.format(Locale.ROOT, "Clima de referencia de %s (no es la ubicación del caballo): hasta %.1f °C "
                + "con %d %% de humedad, índice de estrés térmico %d. %s", zona, temperaturaC, humedad, indice(),
                recomendacion());
    }

    private String recomendacion() {
        if (indice() >= PELIGRO) {
            return "Peligro: evitar el trabajo físico, dar sombra y agua fresca, y consultar al veterinario "
                    + "ante signos de golpe de calor.";
        }
        if (indice() >= RIESGO) {
            return "Riesgo: aumentar el agua fresca disponible, dar sombra y valorar electrolitos con el veterinario.";
        }
        if (indice() >= PRECAUCION) {
            return "Precaución: vigilar el consumo de agua y reducir el esfuerzo en las horas de más calor.";
        }
        return "Sin estrés térmico relevante.";
    }
}
