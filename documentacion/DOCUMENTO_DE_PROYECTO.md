# Documento de proyecto: DietaEquinos

**Corte del documento:** 18 de noviembre de 2026.
**Ubicación del proyecto:** `C:\EquinosFinal`.

Este documento resume qué es el proyecto, cómo está construido, qué se ha hecho hasta ahora, cómo se verificó y qué
falta. Lo esencial para explicarlo está en [GUIA_DE_ESTUDIO.md](GUIA_DE_ESTUDIO.md), el detalle de cada endpoint
en [GUIA_ENDPOINTS.md](GUIA_ENDPOINTS.md) y el recorrido de cada prueba en [GUIA_FLUJO_PRUEBAS.md](GUIA_FLUJO_PRUEBAS.md).

## 1. Estado actual

| Área | Estado | Evidencia |
|---|---|---|
| 4 microservicios | Terminados y funcionando en local | Flujo completo ejecutado el 18 de noviembre |
| CRUD + 5 por servicio | Cumplido en login, registro-caballo y dieta | 29 endpoints públicos |
| 2 APIs externas | Integradas | Pwned Passwords y Open-Meteo probadas contra las APIs reales |
| Notificaciones por correo | Funcionando | 4 correos reales recibidos en una cuenta de Gmail |
| Pruebas unitarias | 387 pruebas sin fallos | Más de 98 % de líneas en cada servicio; el profesor exige 90 % |
| Colección de Postman | Actualizada | Carpeta de notificaciones ejecutada: 9 peticiones y 19 verificaciones correctas |
| Auditoría de código | 12 hallazgos atendidos | Carpeta `anti-slop` |
| Pruebas de carga y estrés | **Pendientes** | El sistema ya tiene los interruptores para hacerlas |
| Despliegue | **Pendiente de decisión** | Ver la sección 12 |
| Subida a GitHub | **Pendiente** | Los repositorios están conectados, pero no tienen commits |

## 2. Qué es el proyecto

**DietaEquinos** genera dietas para caballos con base en los requerimientos nutricionales del NRC (*Nutrient
Requirements of Horses*, 6.ª edición).

Calcular a mano la ración de un caballo es propenso a errores porque depende de su peso, categoría y estado de salud.
El sistema guarda los caballos de cada usuario, calcula la ración diaria en kg de materia seca, forraje y concentrado,
y entrega recomendaciones de qué dar y qué evitar.

**Usuarios previstos:** dueños de caballos, cuidadores de criaderos y estudiantes de zootecnia.

**Límite del sistema:** cada dieta es una recomendación orientativa y no sustituye la valoración de un veterinario.

### Flujo principal

1. El usuario se registra y recibe un correo de bienvenida.
2. Inicia sesión y obtiene un token JWT.
3. Registra un caballo con nombre, código, peso, categoría y estado de salud.
4. Genera la dieta: el sistema elige el perfil del catálogo y calcula la ración con el peso real.
5. Recibe la dieta con un aviso de clima y un correo con el resumen de la ración.

### Catálogo nutricional

Hay **15 perfiles de dieta**, resultado de combinar 3 categorías con 5 estados de salud.

| Categorías | Estados de salud |
|---|---|
| `potrillo`, `joven`, `adulto` | `sin_complicaciones`, `deshidratacion`, `desnutricion`, `laminitis`, `colicos` |

Los perfiles de `sin_complicaciones`, `laminitis` y `colicos` están documentados en las fuentes. Los de
`deshidratacion` y `desnutricion` son inferidos de principios generales, y la API lo advierte en la respuesta.

## 3. Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de los 4 servicios |
| Spring Boot 4.1.0 | Base de cada microservicio |
| Maven (`mvnw.cmd`) | Compilación, pruebas y arranque |
| Spring Web MVC y Validation | API REST y validación de datos |
| Spring Data JPA e Hibernate | Acceso a datos |
| Spring Security y JWT (jjwt 0.12.6) | Autenticación y protección de rutas |
| BCrypt | Encriptación de contraseñas |
| PostgreSQL 18 | Base de datos `Equino_Dieta` |
| Flyway | Creación de esquemas y tablas |
| Spring Mail | Envío de correos por SMTP de Gmail |
| RestTemplate | Comunicación entre servicios y con APIs externas |
| Lombok | Reducción de código repetitivo |
| JUnit 5 y Mockito | Pruebas unitarias con datos simulados |
| Postman y Postman CLI | Pruebas de endpoints |

## 4. Arquitectura

El sistema usa **microservicios** con **arquitectura hexagonal**, organizada igual que el proyecto de clase.

| Paquete | Contenido |
|---|---|
| `domain/model` | Modelos y reglas de negocio |
| `domain/model/gateway` | Interfaces hacia el exterior |
| `domain/usecase` | Casos de uso |
| `domain/exception` | Excepciones de negocio |
| `application/config` | Creación de los casos de uso y configuración |
| `infraestructure/entry_points` | Controladores REST y DTO |
| `infraestructure/driver_adapters` | Implementaciones con JPA, REST, correo o archivos |
| `infraestructure/security` | JWT, filtros y reglas de acceso |

### Microservicios

| Servicio | Puerto | Esquema | Responsabilidad |
|---|---|---|---|
| `login` | 8081 | `auth` | Cuentas, contraseñas, JWT y bloqueo por intentos fallidos |
| `registro-caballo` | 8082 | `caballos` | Caballos de cada usuario. Borrar un caballo es lógico; borrar la cuenta los elimina todos |
| `dieta` | 8083 | `dietas` | Catálogo NRC, cálculo de ración y dietas guardadas |
| `notificaciones` | 8084 | No usa base de datos | Envío de correos a los usuarios |

### Comunicación

| Origen | Destino | Motivo | Protección |
|---|---|---|---|
| `dieta` | `registro-caballo` | Consultar el caballo al generar una dieta | JWT del usuario |
| `login` | `dieta` | Borrar las dietas de una cuenta eliminada | `X-Internal-Key` |
| `login` | `registro-caballo` | Borrar los caballos de una cuenta eliminada | `X-Internal-Key` |
| `login`, `registro-caballo`, `dieta` | `notificaciones` | Avisar eventos por correo | `X-Internal-Key` |
| `login` | Pwned Passwords | Detectar contraseñas filtradas | API pública |
| `dieta` | Open-Meteo | Consultar el clima de referencia | API pública |
| `notificaciones` | Gmail | Enviar correos | Contraseña de aplicación |

Las notificaciones se envían en segundo plano. Si `notificaciones` no responde, la operación del usuario continúa y
solo queda una advertencia en el log.

## 5. Endpoints

### login (`http://localhost:8081`)

| Tipo | Método y ruta | Acceso |
|---|---|---|
| C | `POST /api/usuarios` | Público |
| R | `GET /api/usuarios/{id}` | Token |
| U | `PUT /api/usuarios/{id}` | Token |
| D | `DELETE /api/usuarios/{id}` | Token |
| +1 | `GET /api/usuarios/disponibilidad?correo=` | Público |
| +2 | `POST /api/auth/login` | Público |
| +3 | `GET /api/auth/perfil` | Token |
| +4 | `PUT /api/usuarios/{id}/password` | Token |
| +5 | `POST /api/auth/renovacion` | Token |

### registro-caballo (`http://localhost:8082`)

| Tipo | Método y ruta |
|---|---|
| C | `POST /api/caballos` |
| R | `GET /api/caballos/{id}` |
| R | `GET /api/caballos?page=0&size=10` |
| U | `PUT /api/caballos/{id}` |
| D | `DELETE /api/caballos/{id}` |
| +1 | `POST /api/caballos/{id}/reactivacion` |
| +2 | `PATCH /api/caballos/{id}/estado-salud` |
| +3 | `PATCH /api/caballos/{id}/peso` |
| +4 | `GET /api/caballos/codigo/{codigo}` |
| +5 | `GET /api/caballos/busqueda?categoria=&estado=&page=&size=` |

### dieta (`http://localhost:8083`)

| Tipo | Método y ruta |
|---|---|
| C | `POST /api/dietas` |
| R | `GET /api/dietas/{id}` |
| R | `GET /api/dietas?page=0&size=10` |
| U | `PUT /api/dietas/{id}` |
| D | `DELETE /api/dietas/{id}` |
| +1 | `POST /api/dietas/vista-previa` |
| +2 | `POST /api/dietas/{id}/regeneracion` |
| +3 | `GET /api/dietas/caballo/{caballoId}/vigente` |
| +4 | `GET /api/dietas/caballo/{caballoId}` |
| +5 | `GET /api/dietas/perfiles?categoria=&estado=` |

### Rutas internas (solo entre servicios)

| Servicio | Método y ruta |
|---|---|
| `registro-caballo` | `DELETE /api/internal/caballos/usuario/{usuarioId}` |
| `dieta` | `DELETE /api/internal/dietas/usuario/{usuarioId}` |
| `notificaciones` | `POST /api/internal/notificaciones` |

## 6. Base de datos

Los servicios comparten la base `Equino_Dieta`, pero cada uno trabaja solo en su esquema.

| Esquema | Tablas |
|---|---|
| `auth` | `usuario` |
| `caballos` | `caballo` |
| `dietas` | `categoria`, `estado_salud`, `perfil_dieta`, `perfil_dieta_item`, `dieta` |

Las relaciones y las columnas principales están en la sección "Base de datos" de la guía de estudio.

## 7. Reglas de negocio principales

- **Propiedad de los datos:** cada usuario solo accede a lo suyo. Un recurso ajeno responde 404, igual que uno inexistente.
- **Bloqueo de cuenta:** 3 contraseñas incorrectas seguidas bloquean la cuenta durante 15 minutos (respuesta 423).
- **Contraseñas:** deben tener de 8 a 72 caracteres, no superar 72 bytes y no aparecer en filtraciones conocidas.
- **Código del caballo:** es único entre los caballos activos.
- **Borrado de caballos:** es lógico, y el caballo se puede reactivar.
- **Borrado de cuentas:** primero borra las dietas, luego borra todos los caballos (también los dados de baja) y al final elimina la cuenta. Si un servicio no responde, se detiene con 503 y la cuenta queda intacta.
- **Cálculo de la ración:**
  - Materia seca total = peso × porcentaje del peso corporal.
  - Forraje = materia seca total × porcentaje de forraje.
  - Concentrado = materia seca total − forraje.
- **Historial:** la dieta guarda una copia de los datos del caballo para que no cambie si el caballo se modifica después.

## 8. APIs externas y notificaciones

El requisito era integrar **dos APIs externas**. Las notificaciones son una funcionalidad aparte.

### API 1: Have I Been Pwned, Pwned Passwords

- **Servicio:** `login`, al registrarse y al cambiar la contraseña.
- **Funcionamiento:** calcula el hash SHA-1 y envía solo sus 5 primeros caracteres (k-anonimato). La comparación final se hace en el servidor del proyecto.
- **Si la API falla:** la contraseña se permite y queda una advertencia en el log.
- **Costo:** gratuita y sin clave.

### API 2: Open-Meteo

- **Servicio:** `dieta`, al generar o regenerar una dieta.
- **Funcionamiento:** consulta el clima de un punto fijo de referencia, por defecto Medellín, y agrega un aviso a la dieta.
- **Índice de estrés térmico:** temperatura en °F + porcentaje de humedad, en la hora más calurosa del día.
- **Memoria temporal:** el resultado se guarda 60 minutos, así que muchas dietas generan una sola consulta.
- **Si la API falla:** la dieta se genera sin el aviso de clima.
- **Costo:** gratuita para uso no comercial y sin clave.

| Índice | Nivel |
|---|---|
| Menos de 130 | Sin estrés térmico relevante |
| Desde 130 | Precaución |
| Desde 150 | Riesgo |
| Desde 180 | Peligro |

Este índice es una regla práctica y no un valor del NRC. El clima corresponde a la zona de referencia y no a la
ubicación real del caballo; el aviso lo aclara.

### Notificaciones por correo

La cuenta `pozolesepes@gmail.com` **envía** los correos. Los **recibe** el correo del usuario que realiza la acción.

| Evento | Servicio de origen | Asunto |
|---|---|---|
| Registro de usuario | `login` | Bienvenido a DietaEquinos |
| Cambio de contraseña | `login` | Tu contraseña de DietaEquinos cambió |
| Cambio de estado de salud | `registro-caballo` | Cambió el estado de salud de tu caballo |
| Dieta generada o regenerada | `dieta` | Nueva dieta generada en DietaEquinos |

### Alternativas descartadas

| Alternativa | Motivo |
|---|---|
| Telegram Bot | Exigía vincular cada usuario con su chat y guardar esa relación |
| SMS | Cobra por mensaje y su cuenta de prueba solo envía a números verificados |
| Claude API | El plan Pro no incluye la API, que se paga aparte |

## 9. Seguridad

- Contraseñas guardadas con BCrypt.
- Rutas protegidas con JWT.
- Rutas internas protegidas con `X-Internal-Key`, también frente a URL codificadas.
- La contraseña de aplicación de Gmail solo se define como variable de entorno.
- `.gitignore` excluye `application.properties` y los archivos `.env`. Cada servicio tiene una plantilla `application.properties.example`.
- Se comprobó que la clave de Gmail no quedó en historiales de consola, variables permanentes, archivos del proyecto ni configuración de IntelliJ.

## 10. Pruebas y verificación

### Pruebas unitarias

Siguen las reglas del profesor: JUnit 5 y Mockito, datos simulados con `mock(...)` y `lenient().when(...)`, comentarios
`//Arrange`, `//Act`, `//Assert` y al menos 90 % de las líneas del microservicio completo.

| Servicio | Pruebas | Fallos | Líneas cubiertas | Cobertura |
|---|---|---|---|---|
| `login` | 117 | 0 | 331 de 335 | 98.8 % |
| `registro-caballo` | 100 | 0 | 299 de 300 | 99.7 % |
| `dieta` | 145 | 0 | 510 de 513 | 99.4 % |
| `notificaciones` | 25 | 0 | 88 de 89 | 98.9 % |
| **Total** | **387** | **0** | **1228 de 1237** | **99.3 %** |

La cobertura se midió desde fuera con el agente de JaCoCo, sin agregarlo a los `pom.xml`. IntelliJ puede mostrar
valores un poco distintos.

### Colección de Postman

Para usarla, importa los dos archivos de `postman/importar` y selecciona el entorno `DietaEquinos-local`.

| Carpeta | `postman/importar` (una petición por endpoint) | `postman/collections/Final` (con casos de error) |
|---|---|---|
| `login` | 9 | 37 |
| `registro-caballo` | 10 | 41 |
| `dieta` | 10 | 34 |
| `notificaciones` | 9 | 9 |
| `flujos` | No aplica | 30 |
| **Total** | **38** | **151** |

### Prueba de punta a punta

Se ejecutó con los 4 servicios en local, una base de datos temporal, las APIs reales y envío real por Gmail.

| Paso | Respuesta | Correo |
|---|---|---|
| Registrar usuario | 201 | Recibido |
| Iniciar sesión | 200 | No aplica |
| Registrar caballo | 201 | No aplica |
| Cambiar estado de salud | 200 | Recibido |
| Generar dieta con aviso de clima | 201 | Recibido |
| Cambiar contraseña | 204 | Recibido |

También se comprobó que:

- Una contraseña filtrada se rechaza con 400.
- Sin la clave interna, `notificaciones` responde 401.
- Una dieta se genera aunque `notificaciones` esté apagado.

### Auditoría de código

Se hizo una revisión ligera, adecuada para un equipo que está aprendiendo microservicios. Encontró 12 puntos: 11 se
corrigieron y 1 se ajustó para no modificar la configuración de Gmail. Las correcciones más importantes fueron:

- Las rutas internas se podían llamar sin clave usando una URL codificada.
- Una contraseña de más de 72 bytes producía un error 500; ahora responde 400.
- Se eliminó una colección de Postman que pertenecía a otro proyecto.

El detalle está en [audit-001-2026-11-18.md](../anti-slop/audit-001-2026-11-18.md) y en su
[seguimiento](../anti-slop/audit-001-2026-11-18-seguimiento.md).

## 11. Cronología del trabajo

### 15 de noviembre

- El equipo decidió pausar `notificaciones`, que quedó solo con su estructura de carpetas.

### 16 de noviembre

- Se crearon y ejecutaron las pruebas de Postman de `login`, `registro-caballo` y `dieta`.
- Se generaron los archivos de importación de la colección.
- Se dejó cada servicio en CRUD + 5 y se quitaron los endpoints sobrantes.
- Se simplificó el código para seguir la estructura del proyecto de clase.
- Se limpió la información de las tablas y se rehicieron las pruebas unitarias.
- Se escribieron la guía de estudio y la guía de endpoints.
- Se analizaron las posibles APIs externas.

### 17 de noviembre

- Se rehicieron las pruebas de casos de uso con Mockito y JaCoCo se retiró de los `pom.xml`.
- Las pruebas pasaron a simular también los datos, como pidió el profesor.
- Se compararon Telegram, SMS, correo y las APIs candidatas.

### 18 de noviembre

- Se protegieron las variables de entorno y `application.properties` con `.gitignore`, y se crearon las plantillas.
- Se conectaron los repositorios Git de cada servicio.
- Se extendieron las pruebas a controladores y repositorios, y luego a todo el microservicio, superando el 90 %.
- Se construyó `notificaciones` con Spring Mail.
- Se integraron Pwned Passwords en `login` y Open-Meteo en `dieta`.
- Se conectaron los avisos desde los tres servicios.
- Se evaluaron plataformas de despliegue.
- Se agregó la carpeta `notificaciones` a Postman.
- Se aplicó la auditoría de código.
- Se completó la prueba real de punta a punta con Gmail.

## 12. Decisiones técnicas

| Decisión | Motivo |
|---|---|
| Base de datos compartida con un esquema por servicio | Mantiene los datos separados con una instalación sencilla |
| Sin clave foránea entre esquemas | Cada servicio es dueño de sus tablas |
| Avisos en segundo plano | Un fallo de correo no debe afectar la operación del usuario |
| `notificaciones` sin base de datos | El destinatario llega en cada solicitud |
| Punto fijo para el clima | Evita pedir y guardar la ubicación de cada caballo |
| Hora más calurosa para el índice | El mayor índice del día daba falsas alarmas en madrugadas frías y húmedas |
| Permitir la operación si falla una API externa | Un tercero no debe impedir el uso del sistema |
| Consulta de contraseñas fuera de transacción | Una API lenta no debe retener conexiones a la base de datos |
| Sin JaCoCo en los `pom.xml` | Decisión del equipo; la cobertura se mide desde fuera |

### Plataforma de despliegue (sin decidir)

| Opción | Evaluación |
|---|---|
| Vercel | Descartada: no ejecuta Spring Boot |
| Render gratuito | No sirve para este correo: bloquea el puerto 587 |
| Railway | Según su comunidad, el puerto 587 solo se abre en el plan Pro |
| Oracle Cloud Always Free | Opción recomendada: máquina virtual con Docker Compose |
| Azure for Students | Alternativa: crédito estudiantil y puerto 587 disponible |

## 13. Cómo ejecutar el proyecto

**Requisitos:** Java 21 y PostgreSQL con la base `Equino_Dieta`.

En una consola distinta para cada servicio, dentro de su carpeta:

```bat
mvnw.cmd spring-boot:run
```

### Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `HIBP_ENABLED` | `true` | Activa Pwned Passwords |
| `CLIMA_ENABLED` | `true` | Activa Open-Meteo |
| `CLIMA_LATITUD`, `CLIMA_LONGITUD`, `CLIMA_ZONA` | Medellín | Punto de referencia del clima |
| `CORREO_ENABLED` | `false` | Activa el envío real de correos |
| `GMAIL_USUARIO` | `pozolesepes@gmail.com` | Cuenta que envía los correos |
| `GMAIL_APP_PASSWORD` | Vacío | Contraseña de aplicación de Gmail |

### Configuración para pruebas de carga y estrés

Con estas variables el código se ejecuta completo, pero nada sale a internet:

```bat
set HIBP_ENABLED=false
set CLIMA_ENABLED=false
set CORREO_ENABLED=false
```

## 14. Pendientes y riesgos

### Pendientes

1. **Pruebas de carga y estrés:** faltan por diseñar y ejecutar.
2. **Despliegue:** falta elegir la plataforma y preparar los archivos necesarios.
3. **Subida a GitHub:** ningún servicio tiene commits. Los 4 repositorios apuntan al mismo repositorio remoto, así que hay que definir cómo se organizarán antes del primer `push`.
4. **Clave de Gmail:** debe revocarse cuando termine su uso y generarse otra para la demostración o el despliegue.

### Verificaciones faltantes

- La colección completa de Postman no se volvió a ejecutar después de integrar las APIs y aplicar la auditoría.
- La protección frente a URL codificadas solo se probó con el servicio corriendo en `notificaciones`. En `dieta` y `registro-caballo` está cubierta con pruebas unitarias.
- El rechazo de contraseñas de más de 72 bytes está cubierto con pruebas unitarias, no con una petición real.

### Riesgos conocidos

- **Límite de Gmail:** las cuentas gratuitas tienen un tope diario de envíos, por lo que el correo real debe estar apagado en las pruebas de carga.
- **Catálogo ya cargado:** una base que ya tenga el catálogo conserva el texto anterior, porque el archivo solo se lee con la tabla vacía.
- **Valores de desarrollo:** el secreto JWT, la clave interna y la contraseña de la base tienen valores por defecto solo para uso local. Deben reemplazarse por variables de entorno al desplegar.

## 15. Documentos relacionados

| Documento | Contenido |
|---|---|
| [GUIA_DE_ESTUDIO.md](GUIA_DE_ESTUDIO.md) | Lo esencial para explicar el proyecto: arquitectura, reglas, base de datos, pruebas y preguntas probables |
| [GUIA_FLUJO_PRUEBAS.md](GUIA_FLUJO_PRUEBAS.md) | Cada prueba unitaria: camino feliz o triste, y las clases, métodos y líneas que ejecuta |
| [GUIA_ENDPOINTS.md](GUIA_ENDPOINTS.md) | Entradas, respuestas y errores de cada endpoint |
| [notificaciones/LEEME.md](../notificaciones/LEEME.md) | Activación del envío real de correos |
| [anti-slop/](../anti-slop/) | Informe y seguimiento de la auditoría de código |
| `postman/importar/` | Colección y entorno para importar en Postman |
