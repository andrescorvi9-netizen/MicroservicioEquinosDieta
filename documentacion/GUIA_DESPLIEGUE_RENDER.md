# Guía de despliegue en Render y Neon

Esta guía resume cómo se desplegaron los 4 microservicios de DietaEquinos. Reúne las decisiones del equipo, los pasos que se siguieron y los errores que aparecieron en el camino, con su solución.

Para probar los servicios ya desplegados (Postman, environments, JSON y consultas en Neon), ver [GUIA_POSTMAN_DESPLIEGUE.md](GUIA_POSTMAN_DESPLIEGUE.md).

---

## 1. Qué es desplegar

En local, los 4 microservicios corren en el computador (`localhost:8081` a `8084`) y la base PostgreSQL en `localhost:5432`. Solo funcionan mientras el PC esté encendido, y solo desde ese mismo PC.

**Desplegar** es poner esas aplicaciones a correr en un servidor de internet que está encendido todo el día y tiene una dirección pública.

- **Lo que dijo el docente es cierto:** en Postman la URL ya no es `localhost` sino la del host. Por ejemplo, `http://localhost:8081/api/auth/login` pasa a ser `https://equinos-login.onrender.com/api/auth/login`. Los endpoints, los JSON y los tokens no cambian.
- **No se necesita un front:** se despliega una API REST y Postman hace de cliente.

---

## 2. Decisiones que se tomaron

| Tema | Decisión | Por qué |
|---|---|---|
| Dónde corren los microservicios | **Render** | Despliega desde GitHub y es más barato que Railway para este caso |
| Dónde está la base de datos | **Neon** (PostgreSQL gratis) | La base local no sirve: Render no puede llegar a `localhost` del PC sin exponerlo a internet, y el PC tendría que estar siempre encendido |
| Envío de correos | **Gmail SMTP** con la cuenta `pozolesepes@gmail.com` (sin cambios de código) | Se descartaron Resend y Brevo: piden registro y **no pueden enviar desde una dirección `@gmail.com`** |
| Plan de Render | **Starter de US$7 en los 4 servicios** | Render gratis **bloquea SMTP** (puertos 25, 465 y 587), y los servicios gratis se duermen a los 15 minutos |

### Sobre los precios de Render

En la página de precios aparece **US$25**, pero ese no es el que se necesita:

- **Workspace:** el plan **Hobby cuesta US$0**. El de US$25 es el plan **Pro**, pensado para equipos. **No hay que cambiar a Pro.**
- **Instancia de cada servicio:** la **Starter (0.5 CPU, 512 MB) cuesta US$7/mes**. La de 1 CPU es la otra de US$25.

Total: **4 × US$7 = US$28/mes**, cobrado por segundo de uso. Neon sigue siendo gratis.

Ventajas de tener los 4 en Starter:
- Ningún servicio se duerme, así que no hay que "despertarlos" antes de sustentar.
- Arrancan y responden más rápido.
- Sin el límite de 750 horas gratis al mes.

---

## 3. Archivos que se agregaron al repositorio

Render no tiene Java entre sus lenguajes, así que cada microservicio lleva un **`Dockerfile`** y un **`.dockerignore`** en su carpeta (`login/`, `registro-caballo/`, `dieta/`, `notificaciones/`).

El Dockerfile hace esto:
1. Compila el `.jar` con Maven (Java 21).
2. Como `application.properties` está en `.gitignore` y no está en GitHub, **copia `application.properties.example` como configuración**. Esa plantilla no trae contraseñas: todo sale de las variables de entorno de Render. Si falta una variable obligatoria, el servicio no arranca y el error queda en los logs.
3. Limita la memoria de Java para instancias de 512 MB (`-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k`).
4. No fija el puerto: Render define `PORT` y las propiedades ya lo leen con `${PORT:...}`.

Comandos que se usaron para subirlos (commit `3fc2dff`):

```powershell
cd C:\EquinosFinal
```

```powershell
git add dieta/Dockerfile dieta/.dockerignore login/Dockerfile login/.dockerignore notificaciones/Dockerfile notificaciones/.dockerignore registro-caballo/Dockerfile registro-caballo/.dockerignore
```

```powershell
git commit -m "Agrega Dockerfile y .dockerignore a cada microservicio para desplegar en Render"
```

```powershell
git push origin master
```

**Render solo ve lo que está en GitHub.** Cualquier cambio de código necesita commit y push para desplegarse.

---

## 4. Paso 0: crear la base en Neon

1. Entrar a **neon.tech** y registrarse con **GitHub, Google o correo**.
   - ⚠️ **No usar "Continue with Vercel".** Si se entra por Vercel, Neon crea una organización manejada por Vercel donde no se pueden crear proyectos. Solución: en el selector de organización (arriba a la izquierda) elegir **Create organization** con plan **Free**, o cerrar sesión y registrarse de nuevo sin Vercel.
2. Crear un proyecto:
   - **Name:** `equinos`
   - **Postgres version:** la que venga por defecto
   - **Region:** **AWS US West 2 (Oregon)**, la misma que se usa en Render
3. En **Dashboard → Connect** aparece la cadena de conexión, del tipo:
   ```
   postgresql://neondb_owner:********@ep-spring-dew-arlpjs1k-pooler.c-4.us-west-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
   ```

### Convertir la cadena de Neon en `DB_URL` para Java

| Cambio | Motivo |
|---|---|
| Agregar `jdbc:` al inicio | Java lo necesita |
| Quitar `neondb_owner:contraseña@` | El usuario y la contraseña van en variables aparte |
| Quitar `-pooler` del host | Conexión directa, la recomendada para Flyway |
| Quitar `&channel_binding=require` | Con `sslmode=require` ya va cifrada, y así se evitan incompatibilidades con el driver |

Resultado:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://ep-spring-dew-arlpjs1k.c-4.us-west-2.aws.neon.tech/neondb?sslmode=require` |
| `DB_USER` | `neondb_owner` |
| `DB_PASSWORD` | La contraseña de Neon (empieza con `npg_`). **Nunca se sube a GitHub ni se pega en el chat** |

Plan B si la conexión directa falla: usar el host con `-pooler` (`ep-spring-dew-arlpjs1k-pooler.c-4.us-west-2.aws.neon.tech`).

**No hay que crear tablas en Neon.** Flyway crea solo los esquemas `auth`, `caballos` y `dietas` la primera vez que arranca cada servicio.

---

## 5. Generar las 2 claves secretas

Ejecutar este comando **dos veces** en PowerShell. La primera salida es `JWT_SECRET` y la segunda `INTERNAL_KEY`. Guardarlas en un bloc de notas y **no subirlas a GitHub**.

```powershell
-join ((48..57)+(65..90)+(97..122) | Get-Random -Count 48 | ForEach-Object {[char]$_})
```

- `JWT_SECRET` va **igual** en registro-caballo, dieta y login.
- `INTERNAL_KEY` va **igual** en los 4.

---

## 6. Crear los 4 Web Services en Render

Se crean **en este orden**, porque cada uno necesita la URL de los anteriores:

**1) notificaciones → 2) registro-caballo → 3) dieta → 4) login**

En cada caso: **New + → Web Service → repositorio `MicroservicioEquinosDieta`**.

### Campos comunes a los 4

| Campo | Valor |
|---|---|
| Language | **Docker** |
| Branch | `master` |
| Region | **Oregon (US West)**, la misma en los 4 |
| Dockerfile Path | `./Dockerfile` |
| Docker Build Context Directory | `.` o vacío |
| Instance Type | **Starter (US$7)** |
| Health Check Path (en Advanced) | Vacío |
| Auto-Deploy | **On Commit** (cada push redespliega) |

### Servicio 1: notificaciones (no usa base de datos)

| Campo | Valor |
|---|---|
| Name | `equinos-notificaciones` |
| Root Directory | `notificaciones` |

| Variable | Valor |
|---|---|
| `INTERNAL_KEY` | Clave #2 |
| `GMAIL_APP_PASSWORD` | Contraseña de aplicación de Gmail de **pozolesepes@gmail.com** (16 letras, sin espacios) |
| `CORREO_ENABLED` | `true` |

URL: `https://equinos-notificaciones.onrender.com`

### Servicio 2: registro-caballo

| Campo | Valor |
|---|---|
| Name | **`equinos-registrocaballo`** |
| Root Directory | `registro-caballo` (este sí lleva guion: es el nombre de la carpeta) |

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://ep-spring-dew-arlpjs1k.c-4.us-west-2.aws.neon.tech/neondb?sslmode=require` |
| `DB_USER` | `neondb_owner` |
| `DB_PASSWORD` | Contraseña de Neon |
| `JWT_SECRET` | Clave #1 |
| `INTERNAL_KEY` | Clave #2 |
| `NOTIFICACIONES_URL` | `https://equinos-notificaciones.onrender.com` |

URL: `https://equinos-registrocaballo.onrender.com`

### Servicio 3: dieta

| Campo | Valor |
|---|---|
| Name | `equinos-dieta` |
| Root Directory | `dieta` |

| Variable | Valor |
|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Los mismos de Neon |
| `JWT_SECRET` | Clave #1 |
| `INTERNAL_KEY` | Clave #2 |
| `CABALLOS_URL` | `https://equinos-registrocaballo.onrender.com` |
| `NOTIFICACIONES_URL` | `https://equinos-notificaciones.onrender.com` |

URL: `https://equinos-dieta.onrender.com`

### Servicio 4: login

| Campo | Valor |
|---|---|
| Name | `equinos-login` |
| Root Directory | `login` |

| Variable | Valor |
|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Los mismos de Neon |
| `JWT_SECRET` | Clave #1 |
| `INTERNAL_KEY` | Clave #2 |
| `CABALLOS_URL` | `https://equinos-registrocaballo.onrender.com` |
| `DIETAS_URL` | `https://equinos-dieta.onrender.com` |
| `NOTIFICACIONES_URL` | `https://equinos-notificaciones.onrender.com` |

URL: `https://equinos-login.onrender.com`

### Resumen: qué variable lleva cada servicio

| Variable | notificaciones | registro-caballo | dieta | login |
|---|:-:|:-:|:-:|:-:|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | | ✅ | ✅ | ✅ |
| `JWT_SECRET` | | ✅ | ✅ | ✅ |
| `INTERNAL_KEY` | ✅ | ✅ | ✅ | ✅ |
| `GMAIL_APP_PASSWORD`, `CORREO_ENABLED` | ✅ | | | |
| `NOTIFICACIONES_URL` | | ✅ | ✅ | ✅ |
| `CABALLOS_URL` | | | ✅ | ✅ |
| `DIETAS_URL` | | | | ✅ |

---

## 7. Reglas para no equivocarse

- Escribir los **nombres de las variables a mano y en mayúsculas**. Pegar los **valores sin comillas ni espacios** al inicio o al final.
- `JWT_SECRET` distinto entre servicios → los tokens de login se rechazan (**401**).
- `INTERNAL_KEY` distinta → los servicios no pueden llamarse entre sí (**401/403**, o **503** al borrar un usuario en cascada).
- `DB_PASSWORD` **igual** en registro-caballo, dieta y login. Notificaciones **no** la lleva.
- URLs con `https://` y **sin `/` al final**.
- **No agregar la variable `PORT`.** Render la define sola.
- Al cambiar una variable, confirmar en **Events** que empezó un despliegue **nuevo**. Si no empieza solo: **Manual Deploy → Deploy latest commit**.
- Renombrar un servicio en **Settings → Name** **no cambia su URL**. Para tener otra URL hay que crear el servicio de nuevo con el nombre deseado.
- Si un servicio quedó en Free, se pasa a Starter en **Settings → Instance Type** sin recrearlo.

---

## 8. Cómo saber si un servicio quedó bien

En la pestaña **Logs**:

1. La primera compilación tarda **5 a 10 minutos** (descarga Maven y dependencias).
2. En registro-caballo, dieta y login aparece, solo la primera vez: `Successfully applied 1 migration to schema "..."`.
3. Luego: `Started ...Application in X seconds`.
4. Al final: `Your service is live 🎉` y el estado **Live** en verde.

Si falla, copiar las **últimas 30 o 40 líneas** del log (buscar `ERROR`, `Exception` o `Caused by`) y **borrar cualquier contraseña** antes de compartirlas.

| Si falló en... | Causa probable |
|---|---|
| **Build** (Maven, Docker) | Root Directory mal puesto, o Language distinto de Docker |
| **Deploy** (después de compilar) | Falta una variable, o error de conexión con Neon |
| `Out of memory` | Memoria insuficiente |

> El aviso de Render para conectar su **servidor MCP** a asistentes de IA es opcional. Darle **Dismiss** no afecta el despliegue.

---

## 9. Problemas que aparecieron y cómo se resolvieron

### 9.1 `password authentication failed for user 'neondb_owner'`

Significa que la compilación y la `DB_URL` están bien: falla solo la contraseña.

1. En Neon: **Roles → `neondb_owner` → ⋮ → Reset password**. La contraseña nueva se muestra **una sola vez**. Copiarla con el **botón de copiar** de Neon, no seleccionando con el mouse.
2. (Opcional) Probarla desde pgAdmin o DBeaver: host `ep-spring-dew-arlpjs1k.c-4.us-west-2.aws.neon.tech`, puerto `5432`, base `neondb`, usuario `neondb_owner`, SSL `require`. Si conecta, la contraseña está bien y el problema está en Render.
3. En Render, pestaña **Environment**: **borrar `DB_PASSWORD` y crearla de nuevo**, escribiendo el nombre a mano. Revisar también que `DB_USER` diga exactamente `neondb_owner`.
4. **Save, rebuild, and deploy**.
5. Poner la misma contraseña nueva en **registro-caballo, dieta y login**.

### 9.2 Los correos no llegaban: `Correo simulado (app.correo.enabled=false)`

Si en el log de notificaciones aparece esta línea, el servicio recibe los avisos pero **no los envía**. El valor por defecto de `CORREO_ENABLED` es `false`, así que si la variable falta o está mal escrita, el correo solo queda en el log.

**En este caso la causa fue que `CORREO_ENABLED` estaba mal escrita en Render.** Errores comunes: `CORREO_ENABLE`, `CORREOS_ENABLED`, `CORREO-ENABLED`, minúsculas, un espacio al final, o el valor `"true"` con comillas.

Otros mensajes posibles en el log de notificaciones:

| Si el log dice... | Causa | Solución |
|---|---|---|
| `Correo simulado (app.correo.enabled=false)` | `CORREO_ENABLED` falta o está mal escrita | Crearla de nuevo: `CORREO_ENABLED` = `true` |
| `No fue posible enviar el correo ... Authentication failed` / `535` | `GMAIL_APP_PASSWORD` no es válida para pozolesepes | Generar otra contraseña de aplicación en esa cuenta y pegarla sin espacios |
| `No fue posible enviar el correo ... timeout` / `Couldn't connect` | El servicio está en plan Free (SMTP bloqueado) | Cambiarlo a Starter |
| Nada | El aviso no llegó a notificaciones | Revisar en los logs de login o dieta: `No fue posible avisar al servicio notificaciones` (`INTERNAL_KEY` o `NOTIFICACIONES_URL` mal) |

Datos sobre el correo:
- **Remitente:** `pozolesepes@gmail.com` (`GMAIL_USUARIO`, con ese valor por defecto). Si alguien responde, la respuesta llega a esa misma cuenta.
- La `GMAIL_APP_PASSWORD` tiene que ser una **contraseña de aplicación de esa misma cuenta**, no la contraseña normal.
- **Que el teléfono de pozole no tenga wifi no importa.** Envía el servidor de Render directamente a Gmail. Solo conviene revisar si Google mandó una alerta de "intento de acceso bloqueado".
- El envío es **en segundo plano**: aunque el correo falle, la API responde 201/200. Por eso hay que revisar la bandeja (y spam) o el log.
- Los correos en HTML con banner y logo (commit `ec463f1`) **no causaron el problema**.

### 9.3 `getaddrinfo failed` / "Could not resolve host"

Es una falla momentánea del **DNS de la red del computador** (wifi, router o proveedor), no de Render ni de los microservicios. Basta con reenviar la petición. El equipo decidió mantener el DNS normal. Si pasa el día de la sustentación: reenviar, usar otra red, o cambiar el DNS de Windows a `1.1.1.1` u `8.8.8.8`.

---

## 10. URLs finales

| Servicio | URL |
|---|---|
| login | `https://equinos-login.onrender.com` |
| registro-caballo | `https://equinos-registrocaballo.onrender.com` |
| dieta | `https://equinos-dieta.onrender.com` |
| notificaciones | `https://equinos-notificaciones.onrender.com` |

Con estas URLs se hizo una prueba completa: 21 de 21 pruebas correctas. Confirmó que Neon y Flyway funcionan, que el `JWT_SECRET` coincide, que los servicios se comunican entre sí y que las APIs externas responden, con tiempos de 0,2 a 2,4 segundos.

---

## 11. Antes de la sustentación

Los 4 servicios están en Starter, así que **no se duermen**. Neon gratis sí se duerme tras unos minutos sin uso, pero se despierta solo en cerca de un segundo.

1. Unos minutos antes, enviar una petición a cada servicio desde Postman (o abrir las URLs en el navegador) para confirmar que responden.
2. Hacer un **login de prueba** para confirmar que la base y los tokens funcionan.
3. Revisar en el log de notificaciones que **no aparezca `Correo simulado`**.

> Si en algún momento algún servicio vuelve al plan Free, se duerme tras 15 minutos sin uso y tarda de 1 a 3 minutos en despertar. Este script los despierta:
>
> ```powershell
> $urls = @("https://equinos-registrocaballo.onrender.com","https://equinos-dieta.onrender.com","https://equinos-login.onrender.com"); foreach ($u in $urls) { $t = Measure-Command { try { Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 240 | Out-Null } catch { } }; Write-Host ("$u listo en {0:N0} s" -f $t.TotalSeconds) }
> ```

---

## 12. Después de la sustentación: dejar de pagar

Cuando la nota esté puesta, en cada servicio de Render:
- **Settings → Suspend service**: deja de cobrar y se puede reactivar después, o
- **Settings → Delete service**: si ya no se necesita.

Luego revisar **Billing** para confirmar que no queda nada cobrando.
