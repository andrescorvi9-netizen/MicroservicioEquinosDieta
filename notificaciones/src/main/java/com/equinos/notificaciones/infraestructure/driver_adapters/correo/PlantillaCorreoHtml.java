package com.equinos.notificaciones.infraestructure.driver_adapters.correo;

import java.util.ArrayList;
import java.util.List;

/**
 * Arma la versión HTML del correo: banner con el logo, título, cuerpo y pie.
 * El cuerpo llega como texto plano desde el dominio y aquí solo se le da formato:
 * los párrafos se separan por una línea en blanco y las líneas que empiezan con "- " se muestran como lista.
 * Los estilos van en línea porque la mayoría de clientes de correo ignoran las hojas de estilo.
 */
final class PlantillaCorreoHtml {

    /** Identificador con el que el logo viaja adjunto dentro del correo (se referencia como cid:logo). */
    static final String CID_LOGO = "logo";

    private static final String MARRON = "#3b2a1a";
    private static final String DORADO = "#c8a165";
    private static final String TEXTO = "#2f2f2f";
    private static final String SUAVE = "#8a8178";

    private PlantillaCorreoHtml() {
    }

    static String generar(String asunto, String cuerpo) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f4efe8;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4efe8;padding:24px 12px;">
                <tr><td align="center">
                <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;background:#ffffff;border-radius:10px;overflow:hidden;font-family:Arial,Helvetica,sans-serif;">
                  <tr><td style="background:%1$s;padding:20px 24px;">
                    <table role="presentation" cellpadding="0" cellspacing="0"><tr>
                      <td style="padding-right:16px;"><img src="cid:%5$s" width="110" alt="DietaEquinos" style="display:block;border:0;width:110px;height:auto;"></td>
                      <td>
                        <div style="color:#ffffff;font-size:26px;font-weight:bold;letter-spacing:0.5px;">DietaEquinos</div>
                        <div style="color:%2$s;font-size:14px;margin-top:4px;">Nutrición equina a tu medida</div>
                      </td>
                    </tr></table>
                  </td></tr>
                  <tr><td style="background:%2$s;height:4px;line-height:4px;font-size:0;">&nbsp;</td></tr>
                  <tr><td style="padding:28px 32px 8px 32px;color:%3$s;font-size:15px;line-height:1.6;">
                    <h1 style="margin:0 0 18px 0;font-size:20px;color:%1$s;">%6$s</h1>
                    %7$s
                  </td></tr>
                  <tr><td style="padding:16px 32px 24px 32px;border-top:1px solid #eee4d8;color:%4$s;font-size:12px;line-height:1.5;">
                    DietaEquinos · Sistema de gestión nutricional para caballos
                  </td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(MARRON, DORADO, TEXTO, SUAVE, CID_LOGO, escapar(asunto), cuerpoHtml(cuerpo));
    }

    /** Convierte el texto plano en párrafos y listas HTML. */
    static String cuerpoHtml(String cuerpo) {
        StringBuilder html = new StringBuilder();
        for (String parrafo : cuerpo.split("\\n\\s*\\n")) {
            List<String> texto = new ArrayList<>();
            List<String> lista = new ArrayList<>();
            for (String linea : parrafo.split("\\n")) {
                if (linea.startsWith("- ")) {
                    lista.add(escapar(linea.substring(2)));
                } else if (!linea.isBlank()) {
                    texto.add(escapar(linea));
                }
            }
            if (!texto.isEmpty()) {
                html.append("<p style=\"margin:0 0 14px 0;\">").append(String.join("<br>", texto)).append("</p>");
            }
            if (!lista.isEmpty()) {
                html.append("<ul style=\"margin:0 0 14px 0;padding-left:22px;\">");
                lista.forEach(item -> html.append("<li style=\"margin-bottom:4px;\">").append(item).append("</li>"));
                html.append("</ul>");
            }
        }
        return html.toString();
    }

    /** Evita que un dato del usuario (por ejemplo el nombre de un caballo) se interprete como HTML. */
    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
