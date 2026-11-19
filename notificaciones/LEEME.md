# notificaciones

Envía correos a los usuarios con Spring Mail por SMTP de Gmail. No usa base de datos.

- Puerto: 8084.
- Ruta única: `POST /api/internal/notificaciones`, protegida con el encabezado `X-Internal-Key`. Responde 202 y
  envía el correo en segundo plano.
- La llaman login (bienvenida y contraseña cambiada), registro-caballo (estado de salud cambiado) y dieta (dieta
  generada). Si este servicio no está corriendo, ellos siguen funcionando y solo registran una advertencia.

## Activar el envío real

Por defecto `CORREO_ENABLED=false`: no se conecta a Gmail y solo registra el asunto en el log.

1. En la cuenta de Gmail del proyecto, activa la verificación en dos pasos.
2. Genera una contraseña de aplicación en https://myaccount.google.com/apppasswords.
3. Define estas variables de entorno antes de arrancar el servicio (nunca las escribas en un archivo del repositorio):

| Variable | Valor |
|---|---|
| `GMAIL_USUARIO` | Cuenta remitente (por defecto `pozolesepes@gmail.com`) |
| `GMAIL_APP_PASSWORD` | Contraseña de aplicación de 16 caracteres, sin espacios |
| `CORREO_ENABLED` | `true` |

En las pruebas de carga y estrés deja `CORREO_ENABLED=false`: Gmail limita los envíos diarios y puede bloquear la cuenta.
