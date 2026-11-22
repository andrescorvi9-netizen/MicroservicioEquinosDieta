# Activar las notificaciones por correo

Comandos de CMD para arrancar el microservicio `notificaciones` con el envío real de correos por Gmail.

La cuenta `pozolesepes@gmail.com` **envía** los correos. Los **recibe** el correo del usuario que realiza la acción en
el sistema.

## 1. Requisitos

- **Java 21** instalado.
- **Contraseña de aplicación** de la cuenta de Gmail. No es la contraseña normal de la cuenta.
- **Puerto 8084 libre.** Si `notificaciones` ya está corriendo en IntelliJ, detenlo antes con el botón rojo **Stop**.

### Generar la contraseña de aplicación

1. Abre la cuenta `pozolesepes@gmail.com` en el navegador.
2. Activa la [verificación en dos pasos](https://myaccount.google.com/signinoptions/twosv).
3. Entra a [contraseñas de aplicación](https://myaccount.google.com/apppasswords).
4. Escribe `DietaEquinos` como nombre y pulsa **Crear**.
5. Copia la clave de 16 caracteres. Google solo la muestra una vez.

## 2. Arrancar notificaciones con envío real

Abre CMD y ejecuta los comandos uno por uno, en la misma ventana.

Ir a la carpeta del servicio:

```bat
cd /d C:\EquinosFinal\notificaciones
```

Escribir la clave. El comando la pide, así que no queda escrita dentro del comando:

```bat
set /p GMAIL_APP_PASSWORD=Pega la clave sin espacios y pulsa Enter: 
```

Activar el envío real:

```bat
set CORREO_ENABLED=true
```

Arrancar el servicio:

```bat
mvnw.cmd spring-boot:run
```

- **Formato de la clave:** pégala **sin espacios**. Google la muestra en 4 grupos de 4 letras, así que debes juntarlos.
- **Servicio listo:** cuando aparece `Started NotificacionesApplication`, ya está escuchando en el puerto 8084.
- **Deja la ventana abierta:** si la cierras, el servicio se apaga.

## 3. Arrancar los demás servicios

Los avisos se generan en `login`, `registro-caballo` y `dieta`. Arranca cada uno en **su propia ventana de CMD**.

`login` (puerto 8081):

```bat
cd /d C:\EquinosFinal\login
mvnw.cmd spring-boot:run
```

`registro-caballo` (puerto 8082):

```bat
cd /d C:\EquinosFinal\registro-caballo
mvnw.cmd spring-boot:run
```

`dieta` (puerto 8083):

```bat
cd /d C:\EquinosFinal\dieta
mvnw.cmd spring-boot:run
```

Estos tres servicios no necesitan la clave de Gmail. Solo envían el aviso a `notificaciones`, que es quien escribe a Gmail.

## 4. Correos que se envían

| Acción | Servicio | Asunto del correo |
|---|---|---|
| Registrar usuario | `login` | Bienvenido a DietaEquinos |
| Cambiar contraseña | `login` | Tu contraseña de DietaEquinos cambió |
| Cambiar el estado de salud de un caballo | `registro-caballo` | Cambió el estado de salud de tu caballo |
| Generar o regenerar una dieta | `dieta` | Nueva dieta generada en DietaEquinos |

El correo de estado de salud solo se envía si el estado nuevo es distinto del anterior.

Para probar sin los otros servicios, usa la carpeta `notificaciones` de la colección de Postman. Envía los correos a la
variable `correo_notificacion` del entorno `DietaEquinos-local`, y cada ejecución completa de la carpeta manda 4 correos
reales.

## 5. Comprobar el resultado en la ventana de notificaciones

| Mensaje | Significado |
|---|---|
| `Started NotificacionesApplication` | El servicio arrancó |
| `Correo simulado (app.correo.enabled=false)` | El envío real está apagado: falta `set CORREO_ENABLED=true` en esa ventana |
| `No fue posible enviar el correo` | Gmail rechazó el envío: revisa la clave de aplicación |
| `Port 8084 was already in use` | Otro proceso usa el puerto: detén `notificaciones` en IntelliJ o en otra ventana |

Si el correo no aparece en la bandeja de entrada, revisa la carpeta **Spam**.

Para ver qué proceso ocupa el puerto 8084:

```bat
netstat -ano | findstr :8084
```

El último número de la línea es el PID del proceso. Lo puedes buscar en el Administrador de tareas, en la pestaña
**Detalles**.

## 6. Apagar y limpiar

1. En la ventana de `notificaciones`, pulsa **`Ctrl + C`**.
2. Si pregunta `¿Desea terminar el trabajo por lotes (S/N)?`, escribe **`S`** y pulsa **Enter**.
3. Cierra la ventana. Así también se borra la clave de esa sesión:

```bat
exit
```

4. Si ya no vas a enviar correos, [revoca la clave](https://myaccount.google.com/apppasswords) con el icono de la
   papelera. Para volver a usar el servicio, genera una nueva.

## 7. Arrancar sin enviar correos

Para pruebas de carga y estrés, o cuando no quieras enviar correos reales, no definas la clave y deja el envío apagado.
El servicio recibe los avisos y solo los registra en el log:

```bat
cd /d C:\EquinosFinal\notificaciones
set CORREO_ENABLED=false
mvnw.cmd spring-boot:run
```

En las pruebas de carga, apaga también las APIs externas en las ventanas de `login` y `dieta`, antes de
`mvnw.cmd spring-boot:run`:

```bat
set HIBP_ENABLED=false
set CLIMA_ENABLED=false
```

## Reglas de seguridad

- No escribas la clave en `application.properties`, en Postman ni en ningún archivo del proyecto.
- No la compartas por chat ni la subas al repositorio.
- Cada persona del equipo debe pedir su propia clave o pedir a quien administra la cuenta que arranque el servicio.
