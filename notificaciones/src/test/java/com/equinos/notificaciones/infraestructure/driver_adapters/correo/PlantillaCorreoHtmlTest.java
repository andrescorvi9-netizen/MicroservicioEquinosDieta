package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas de PlantillaCorreoHtml con textos simulados. */
class PlantillaCorreoHtmlTest {

    @Test
    void generaElBannerConElLogoYElTitulo() {
        //Act
        String html = PlantillaCorreoHtml.generar("Nueva dieta generada", "Hola Ana:");

        //Assert
        assertTrue(html.contains("src=\"cid:logo\""));
        assertTrue(html.contains("DietaEquinos"));
        assertTrue(html.contains("<h1"));
        assertTrue(html.contains("Nueva dieta generada"));
        assertTrue(html.contains("<p style=\"margin:0 0 14px 0;\">Hola Ana:</p>"));
    }

    @Test
    void separaParrafosYConvierteLasLineasConGuionEnLista() {
        //Arrange
        String cuerpo = "Hola Ana:\n\nRación diaria:\n- Total: 10 kg\n- Forraje: 8 kg\n\nFin.";

        //Act
        String html = PlantillaCorreoHtml.cuerpoHtml(cuerpo);

        //Assert
        assertEquals("<p style=\"margin:0 0 14px 0;\">Hola Ana:</p>"
                + "<p style=\"margin:0 0 14px 0;\">Ración diaria:</p>"
                + "<ul style=\"margin:0 0 14px 0;padding-left:22px;\">"
                + "<li style=\"margin-bottom:4px;\">Total: 10 kg</li>"
                + "<li style=\"margin-bottom:4px;\">Forraje: 8 kg</li>"
                + "</ul>"
                + "<p style=\"margin:0 0 14px 0;\">Fin.</p>", html);
    }

    @Test
    void unParrafoDeVariasLineasLasUneConSaltoDeLinea() {
        //Act
        String html = PlantillaCorreoHtml.cuerpoHtml("Línea uno\nLínea dos");

        //Assert
        assertEquals("<p style=\"margin:0 0 14px 0;\">Línea uno<br>Línea dos</p>", html);
    }

    @Test
    void unaListaSinTextoNoGeneraParrafoVacio() {
        //Act
        String html = PlantillaCorreoHtml.cuerpoHtml("- uno\n   \n- dos");

        //Assert
        assertFalse(html.contains("<p"));
        assertTrue(html.contains("<li style=\"margin-bottom:4px;\">uno</li>"));
    }

    @Test
    void escapaLosCaracteresEspecialesDeHtml() {
        //Act
        String html = PlantillaCorreoHtml.generar("<b>", "Caballo <script>\"Rayo\" & Cía</script>");

        //Assert
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("Caballo &lt;script&gt;&quot;Rayo&quot; &amp; Cía&lt;/script&gt;"));
        assertTrue(html.contains("&lt;b&gt;"));
    }
}
