# Guía de estudio: DietaEquinos

Lo esencial para explicar el proyecto ante el docente. Para más detalle:

- [GUIA_FLUJO_PRUEBAS.md](GUIA_FLUJO_PRUEBAS.md): qué código ejecuta cada prueba unitaria.
- [GUIA_ENDPOINTS.md](GUIA_ENDPOINTS.md): entradas, respuestas y errores de cada endpoint.
- [DOCUMENTO_DE_PROYECTO.md](DOCUMENTO_DE_PROYECTO.md): estado, cronología, decisiones y pendientes.

## 1. El proyecto en un minuto

**DietaEquinos** calcula la dieta diaria de un caballo según el NRC (*Nutrient Requirements of Horses*), la guía de
referencia en nutrición equina.

1. El usuario se registra y recibe un correo de bienvenida.
2. Inicia sesión y recibe un **token JWT**, una "credencial" que envía en cada petición.
3. Registra su caballo con peso, categoría y estado de salud.
4. Pide una dieta. El sistema elige el perfil del catálogo y calcula la ración con el peso real del caballo.
5. Recibe la dieta, un aviso de clima y un correo con el resumen.

La dieta es orientativa: no reemplaza a un veterinario, y la respuesta lo advierte.

**Catálogo:** 3 categorías × 5 estados de salud = **15 perfiles**.

| Categorías | Estados de salud |
|---|---|
| `potrillo`, `joven`, `adulto` | `sin_complicaciones`, `deshidratacion`, `desnutricion`, `laminitis`, `colicos` |

Los perfiles de `deshidratacion` y `desnutricion` son **inferidos**: las fuentes no los documentan, y la API lo avisa.

## 2. Tecnologías

| Tecnología | Para qué se usa |
|---|---|
| Java 21 y Spring Boot 4.1.0 | Base de los 4 servicios. |
| Maven (`mvnw.cmd`) | Compilar, probar y arrancar cada servicio. |
| Spring Web MVC | Crear la API REST: rutas que responden por HTTP. |
| Spring Data JPA | Leer y guardar en la base de datos sin escribir SQL a mano. |
| PostgreSQL y Flyway | La base de datos, y los scripts que crean sus tablas al arrancar. |
| Spring Security, JWT y BCrypt | Proteger las rutas, identificar al usuario y guardar las contraseñas cifradas. |
| RestTemplate | Llamar a otros servicios y a las APIs externas. |
| Spring Mail | Enviar correos por Gmail. |
| JUnit 5 y Mockito | Pruebas unitarias con datos simulados. |
| Postman | Probar los endpoints con peticiones reales. |

## 3. Arquitectura

### Microservicios

Un **microservicio** es una aplicación pequeña con una sola tarea y su propio puerto.

| Servicio | Puerto | Esquema de la base | Qué hace |
|---|---|---|---|
| `login` | 8081 | `auth` | Cuentas, contraseñas, token JWT y bloqueo por intentos fallidos. |
| `registro-caballo` | 8082 | `caballos` | Caballos de cada usuario. El borrado es lógico. |
| `dieta` | 8083 | `dietas` | Catálogo NRC, cálculo de la ración y dietas guardadas. |
| `notificaciones` | 8084 | No usa base | Envía los correos. |

Los tres primeros comparten la base `Equino_Dieta`, pero cada uno solo toca su **esquema** (su grupo de tablas).

### Arquitectura hexagonal

Cada servicio separa el negocio de la tecnología. El negocio (`domain`) no sabe si los datos vienen de PostgreSQL, de
otro servicio o de un mock: solo conoce **interfaces** llamadas *gateways*.

| Capa | Carpeta | Qué contiene | Ejemplo en login |
|---|---|---|---|
| Modelo | `domain/model` | Datos y reglas del negocio. | `Usuario`: bloquea la cuenta tras 3 intentos. |
| Gateway | `domain/model/gateway` | Interfaces hacia el exterior. | `UsuarioGateway`: guardar, buscar, borrar. |
| Caso de uso | `domain/usecase` | Los pasos de cada operación. | `AutenticacionUseCase.iniciarSesion`. |
| Configuración | `application/config` | Crea los casos de uso y les entrega sus gateways. | `UsuarioConfig`. |
| Entrada | `infraestructure/entry_points` | Controladores REST: reciben la petición HTTP. | `AuthController`. |
| Adaptadores | `infraestructure/driver_adapters` | Implementan los gateways con JPA, HTTP o correo. | `UsuarioDataGatewayImpl`. |
| Seguridad | `infraestructure/security` | JWT, filtros y reglas de acceso. | `SecurityConfig`. |

### Recorrido de una petición: iniciar sesión

`POST /api/auth/login` con correo y contraseña:

1. `AuthController.login` recibe la petición y llama al caso de uso.
2. `AutenticacionUseCase.iniciarSesion` busca el usuario por correo con `UsuarioGateway`.
3. Revisa si la cuenta está bloqueada y compara la contraseña con BCrypt.
4. Si es correcta, reinicia los intentos fallidos y pide el token a `TokenGateway`.
5. El controlador devuelve el token en JSON.

```java
// AutenticacionUseCase.java
34   if (!hasher.coincide(password, usuario.getPasswordHash())) {
35       boolean quedoBloqueada = usuario.registrarIntentoFallido(ahora);
36       usuarios.guardar(usuario);
         ...
44   usuario.registrarIngresoExitoso(ahora);
45   Usuario guardado = usuarios.guardar(usuario);
46   return new SesionIniciada(guardado, tokens.emitir(guardado, ahora));
```

El caso de uso nunca habla con PostgreSQL directamente: usa `usuarios`, que es la interfaz `UsuarioGateway`. Spring le
entrega la implementación real (`UsuarioDataGatewayImpl`) y las pruebas le entregan un mock.

## 4. Comunicación y seguridad

| Origen | Destino | Para qué | Cómo se protege |
|---|---|---|---|
| `dieta` | `registro-caballo` | Consultar el caballo al generar una dieta. | Con el JWT del usuario. |
| `login` | `dieta` y `registro-caballo` | Borrar dietas y caballos al eliminar una cuenta. | `X-Internal-Key` |
| `login`, `registro-caballo`, `dieta` | `notificaciones` | Pedir el envío de un correo. | `X-Internal-Key` |
| `login` | Pwned Passwords | Detectar contraseñas filtradas. | API pública, sin clave. |
| `dieta` | Open-Meteo | Consultar el clima. | API pública, sin clave. |

- **JWT:** login lo firma al iniciar sesión. Los demás servicios comprueban la firma con el mismo secreto. Del token sale
  el id del usuario, así que nadie puede pedir datos de otro.
- **`X-Internal-Key`:** encabezado con una clave que solo conocen los servicios. Protege las rutas `/api/internal/...`.
- **BCrypt:** la contraseña se guarda como un *hash*, un texto del que no se puede recuperar la original.
- **Recurso ajeno = 404:** si un usuario pide algo de otro, la respuesta es la misma que si no existiera. Así no se
  revela qué datos existen.

## 5. Reglas de negocio

| Regla | Dónde está |
|---|---|
| 3 contraseñas incorrectas seguidas bloquean la cuenta 15 minutos (respuesta 423). | `Usuario.registrarIntentoFallido` |
| La contraseña debe tener de 8 a 72 caracteres, no pasar de 72 bytes y no estar filtrada. | `UsuarioUseCase` |
| El código del caballo es único entre los caballos activos. | `CaballoUseCase` |
| Borrar un caballo solo lo marca como inactivo, y se puede reactivar. | `Caballo.desactivar` y `Caballo.reactivar` |
| Eliminar una cuenta borra las dietas, luego borra de la tabla todos los caballos (también los dados de baja) y al final borra la cuenta. Si un servicio no responde, se detiene con 503 y la cuenta queda intacta. | `UsuarioUseCase.eliminar` |
| La dieta guarda una copia del caballo, para que el historial no cambie si el caballo cambia. | `DietaUseCase.generar` |

**Cálculo de la ración** (`Racion.calcular`), en kg de materia seca (lo que queda del alimento sin el agua):

```
materia seca total = peso × % del peso corporal
forraje            = materia seca total × % de forraje
concentrado        = materia seca total − forraje
```

Ejemplo: un caballo adulto de 500 kg con laminitis (1.5 %, 95 % de forraje) come 7.50 kg de materia seca: 7.12 de
forraje y 0.38 de concentrado.

## 6. APIs externas y notificaciones

El requisito era integrar **dos APIs externas**. Si alguna falla, la operación del usuario sigue y solo queda una
advertencia en el log.

**Pwned Passwords** (en `login`, al registrarse y al cambiar la contraseña)

- Revisa si la contraseña apareció en filtraciones conocidas.
- No envía la contraseña. Envía solo los 5 primeros caracteres de su *hash* SHA-1, y la comparación final se hace en
  `PasswordFiltradaGatewayImpl`.

**Open-Meteo** (en `dieta`, al generar o regenerar una dieta)

- Consulta el clima de un punto de referencia, por defecto Medellín, y agrega un aviso a la dieta.
- **Índice de estrés térmico** = temperatura en °F + % de humedad, en la hora más calurosa del día. Desde 130 pide
  precaución, desde 150 hay riesgo y desde 180 hay peligro (`ClimaDia`).
- Guarda el resultado 60 minutos para no consultar en cada dieta.

**Notificaciones por correo**

| Evento | Servicio que avisa |
|---|---|
| Bienvenida al registrarse | `login` |
| Contraseña cambiada | `login` |
| Estado de salud del caballo cambiado | `registro-caballo` |
| Dieta generada o regenerada | `dieta` |

El aviso se envía **en segundo plano**, con un grupo de hilos (`NotificacionesConfig`). Un hilo es una tarea que corre
en paralelo: así el usuario no espera el correo, y si `notificaciones` está apagado la operación igual termina.

## 7. Base de datos

| Esquema | Tablas |
|---|---|
| `auth` | `usuario` |
| `caballos` | `caballo` |
| `dietas` | `categoria`, `estado_salud`, `perfil_dieta`, `perfil_dieta_item`, `dieta` |

Hay dos tipos de relación:

| Tipo | Ejemplo | Cómo se cuida |
|---|---|---|
| **Real**: clave foránea en la base. | `perfil_dieta` → `categoria` y `estado_salud`; `perfil_dieta_item` → `perfil_dieta`; `dieta` → `perfil_dieta`. | PostgreSQL no deja guardar un valor que no exista. |
| **Lógica**: solo un número guardado. | `caballo.usuario_id` → `usuario`; `dieta.caballo_id` → `caballo`. | Por código: el id sale del token, `dieta` consulta el caballo y la eliminación de la cuenta borra en cascada. |

Las relaciones entre servicios son lógicas porque cada servicio es dueño de su esquema y no puede apuntar a las tablas
de otro.

**Columnas que conviene recordar:**

- `usuario`: `correo` (único), `password_hash`, `intentos_fallidos`, `bloqueado_hasta`.
- `caballo`: `codigo` (único entre activos), `peso_kg`, `categoria`, `estado_salud`, `activo`.
- `dieta`: copia del caballo (`caballo_nombre`, `peso_kg`, ...), `perfil_id` y la ración (`ms_total_kg`, `forraje_kg`,
  `concentrado_kg`).

## 8. Endpoints

Cada servicio cumple **CRUD + 5**: crear, leer, actualizar y borrar, más 5 endpoints de negocio.

| Servicio | Ejemplos de negocio |
|---|---|
| `login` | Iniciar sesión, renovar el token, ver el perfil, cambiar la contraseña, consultar si un correo está libre. |
| `registro-caballo` | Reactivar, cambiar el estado de salud, cambiar el peso, buscar por código, búsqueda con filtros. |
| `dieta` | Vista previa sin guardar, regenerar, dieta vigente, historial del caballo, catálogo de perfiles. |

En total hay **29 endpoints públicos** y **3 rutas internas** (`/api/internal/...`). El detalle está en
[GUIA_ENDPOINTS.md](GUIA_ENDPOINTS.md).

## 9. Pruebas unitarias

Una **prueba unitaria** revisa una sola clase, sin base de datos, sin red y sin arrancar Spring.

### Reglas del profesor

- Usar JUnit 5 y Mockito.
- Simular también los datos con `mock(...)` y `lenient().when(...)`, para no hacer consultas extras.
- Separar cada prueba en `//Arrange`, `//Act` y `//Assert`.
- Cubrir al menos el **90 % de las líneas** del microservicio completo.

### Ejemplo

```java
@Test
void ObtenerCuentaPropia() {
    //Arrange: se prepara un usuario simulado y lo que responderá el gateway simulado
    Usuario usuario = usuarioSimulado();
    when(usuarios.buscarPorId(ID)).thenReturn(Optional.of(usuario));

    //Act: se ejecuta el método que se prueba
    Usuario usuarioResultado = useCase.obtener(ID, ID);

    //Assert: se comprueba el resultado
    assertSame(usuario, usuarioResultado);
}
```

- **Mock:** objeto falso creado por Mockito. Responde lo que se le indica con `when(...)` y luego se puede revisar cómo
  se usó con `verify(...)`.
- **`lenient()`:** marca un dato simulado que algunas pruebas no usan. Sin él, Mockito falla si sobra una simulación.
- **Camino feliz:** la operación sale bien. **Camino triste:** hay un error, un rechazo o falta un dato.
- **Clases reales en las pruebas:** `UsuarioTest`, `CaballoTest` y las demás pruebas de modelos usan el objeto real,
  porque es la clase que se prueba. Si fuera un mock, no se ejecutaría su código.

### Cifras

| Servicio | Pruebas | Líneas cubiertas |
|---|---|---|
| `login` | 117 | 98.8 % |
| `registro-caballo` | 100 | 99.7 % |
| `dieta` | 145 | 99.4 % |
| `notificaciones` | 25 | 98.9 % |
| **Total** | **387** | **99.3 %** |

La cobertura se mide con JaCoCo desde fuera del proyecto, sin agregarlo a los `pom.xml`. IntelliJ puede mostrar
valores un poco distintos. En [GUIA_FLUJO_PRUEBAS.md](GUIA_FLUJO_PRUEBAS.md) está, para cada prueba, si es camino
feliz o triste y qué clases, métodos y líneas ejecuta.

## 10. Preguntas probables del docente

**¿Por qué microservicios?** Cada parte del negocio crece y falla por separado. Si `notificaciones` se cae, los
usuarios siguen generando dietas.

**¿Por qué arquitectura hexagonal?** El negocio no depende de la tecnología. Se puede cambiar PostgreSQL o probar con
mocks sin tocar los casos de uso.

**¿Cómo sabe registro-caballo quién es el usuario?** Lee el id del token JWT, que login firmó. No confía en un id que
llegue en la URL.

**¿Qué pasa si se elimina una cuenta y dieta no responde?** Se detiene con 503 antes de borrar la cuenta, así que no
quedan caballos ni dietas sin dueño.

**¿Por qué no hay claves foráneas entre esquemas?** Cada servicio es dueño de sus tablas. Las relaciones se cuidan por
código.

**¿Cómo se guarda la contraseña?** Con BCrypt. Ni siquiera el sistema puede ver la contraseña original.

**¿Qué pasa si una API externa falla?** La operación sigue: la contraseña se acepta y la dieta sale sin aviso de clima.
Queda una advertencia en el log.

**¿Por qué el correo va en segundo plano?** Para que el usuario no espere a Gmail y para que un fallo del correo no
deshaga la operación.

**¿Qué es un mock y por qué se usa?** Es un objeto simulado. Permite probar un caso de uso sin base de datos ni red, y
provocar errores difíciles de lograr en la realidad, como que dieta no responda.

## 11. Glosario

| Término | Significado |
|---|---|
| API REST | Forma de comunicarse por HTTP con rutas y métodos: GET, POST, PUT, PATCH, DELETE. |
| Endpoint | Una ruta con su método, por ejemplo `POST /api/caballos`. |
| JSON | Formato de texto en el que viajan los datos. |
| Código HTTP | Número que dice cómo salió la petición: 200 bien, 201 creado, 204 sin contenido, 400 datos inválidos, 401 sin token, 404 no existe, 409 conflicto, 423 bloqueado, 503 servicio no disponible. |
| DTO | Clase que solo transporta datos de una petición o respuesta, como `LoginRequest`. |
| Entidad JPA | Clase que representa una tabla, como `UsuarioData`. |
| Mapper | Clase que copia datos entre el modelo y la entidad. |
| Hash | Resultado de un cálculo de un solo sentido: no se puede volver al texto original. |
| Borrado lógico | Marcar la fila como inactiva en lugar de borrarla. |
| Paginación | Entregar una lista por partes con `page` y `size`. |
| Cobertura | Porcentaje de líneas del código que las pruebas ejecutan. |
