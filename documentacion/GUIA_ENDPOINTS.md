# Funcionamiento de cada endpoint

Complemento de la guía de estudio (`GUIA_DE_ESTUDIO.md`). Explica qué recibe, qué hace, qué responde y qué
errores puede dar cada endpoint del CRUD + 5 de login, registro-caballo y dieta, las dos rutas internas de borrado
y la ruta interna de notificaciones.
Todo sale de los controladores, los DTO, los casos de uso y `GlobalExceptionHandler` del código actual.

## Reglas comunes a los tres servicios

- **Token.** Salvo las rutas marcadas como *públicas*, todas piden el encabezado
  `Authorization: Bearer <token>` con el JWT que entrega `POST /api/auth/login`. Sin token, o con un token
  alterado o vencido, la respuesta es **401** con el mensaje "Token ausente, inválido o vencido".
- **Solo lo propio.** Cada usuario solo ve y cambia lo suyo. Un recurso de otro usuario responde **404**, igual
  que uno que no existe, para no revelar que existe.
- **Validación.** Si el cuerpo no cumple las reglas del DTO (campo vacío, demasiado largo, formato inválido), la
  respuesta es **400** con la lista de campos en `detalles`. Un JSON mal escrito o un id no numérico (por
  ejemplo `/api/caballos/abc`) también da 400.
- **Formato de error.** Todos los errores llegan con el mismo cuerpo (`ErrorApi`):

```json
{
  "timestamp": "2026-11-16T15:00:00Z",
  "status": 404,
  "error": "Not Found",
  "mensaje": "Caballo no encontrado",
  "ruta": "/api/caballos/999",
  "detalles": []
}
```

- **Paginación.** Las listas reciben `page` (desde 0, por defecto 0) y `size` (de 1 a 100, por defecto 10).
  Fuera de esos límites la respuesta es 400. La página llega así (`PaginaResponse`):

```json
{ "contenido": [ ... ], "pagina": 0, "tamano": 10, "totalElementos": 1, "totalPaginas": 1 }
```

| Código | Significado en este proyecto |
|---|---|
| 200 | Correcto, con cuerpo. |
| 201 | Se creó un recurso (usuario, caballo o dieta). |
| 204 | Correcto, sin cuerpo (eliminar, cambiar contraseña). |
| 400 | Datos inválidos o regla de negocio incumplida. |
| 401 | Sin token, token inválido, credenciales incorrectas o clave interna incorrecta. |
| 404 | No existe o es de otro usuario. |
| 409 | Conflicto: correo o código repetido, caballo ya activo. |
| 423 | Cuenta bloqueada por intentos fallidos. |
| 503 | Otro microservicio no respondió. |

---

# login (puerto 8081)

| Tipo | Método y ruta | Acceso | Respuesta correcta |
|---|---|---|---|
| C | `POST /api/usuarios` | Pública | 201 + usuario |
| R | `GET /api/usuarios/{id}` | Token | 200 + usuario |
| U | `PUT /api/usuarios/{id}` | Token | 200 + usuario |
| D | `DELETE /api/usuarios/{id}` | Token | 204 |
| +1 | `GET /api/usuarios/disponibilidad?correo=` | Pública | 200 |
| +2 | `POST /api/auth/login` | Pública | 200 + token |
| +3 | `GET /api/auth/perfil` | Token | 200 + usuario |
| +4 | `PUT /api/usuarios/{id}/password` | Token | 204 |
| +5 | `POST /api/auth/renovacion` | Token | 200 + token nuevo |

La respuesta "usuario" (`UsuarioResponse`) nunca incluye la contraseña:

```json
{ "id": 1, "nombre": "Ana Torres", "correo": "ana@correo.com",
  "creadoEn": "2026-11-16T15:00:00Z", "actualizadoEn": "2026-11-16T15:00:00Z" }
```

## C. Registrar usuario: `POST /api/usuarios`

- **Acceso:** pública (no pide token).
- **Recibe** (`RegistroUsuarioRequest`):

```json
{ "nombre": "Ana Torres", "correo": "ana@correo.com", "password": "EquinoDietaPrueba7319Xq" }
```

  `nombre` obligatorio (máximo 120), `correo` con formato de correo (máximo 160), `password` de 8 a 72 caracteres y máximo 72 bytes (las tildes y la ñ ocupan más de uno).
- **Qué hace:** guarda el correo en minúsculas y sin espacios, comprueba con la API Pwned Passwords que la contraseña
  no aparezca en filtraciones, la encripta con BCrypt, crea la cuenta y envía el correo de bienvenida
  (`UsuarioUseCase.registrar`). Si la API externa no responde, la contraseña se permite.
- **Responde:** 201 con el usuario.
- **Errores:** 400 si los datos no son válidos o si la contraseña aparece en filtraciones; 409 si ya existe una
  cuenta con ese correo.

## R. Consultar usuario: `GET /api/usuarios/{id}`

- **Acceso:** token. Solo la cuenta propia.
- **Qué hace:** devuelve los datos de la cuenta si `{id}` es el del usuario del token (`UsuarioUseCase.obtener`).
- **Responde:** 200 con el usuario.
- **Errores:** 404 si el id es de otra cuenta o no existe; 400 si el id no es un número.

## U. Actualizar usuario: `PUT /api/usuarios/{id}`

- **Acceso:** token. Solo la cuenta propia.
- **Recibe** (`ActualizarUsuarioRequest`): `{ "nombre": "Ana María Torres", "correo": "ana@correo.com" }`, con
  las mismas reglas que el registro.
- **Qué hace:** cambia el nombre y el correo. Si el correo cambia, revisa que no lo use otra cuenta. Si el nombre o el
  correo cambiaron, avisa por correo con los datos nuevos; si cambió el correo, el aviso llega a la dirección nueva y
  también a la anterior, para que el dueño se entere aunque no haya sido él.
- **Responde:** 200 con el usuario actualizado.
- **Errores:** 400 datos inválidos; 404 cuenta ajena; 409 correo usado por otra cuenta.

## D. Eliminar usuario: `DELETE /api/usuarios/{id}`

- **Acceso:** token. Solo la cuenta propia.
- **Qué hace:** borrado en cascada (`UsuarioUseCase.eliminar`):
  1. Comprueba que la cuenta existe y es la propia.
  2. Pide a dieta que borre todas las dietas del usuario (ruta interna).
  3. Pide a registro-caballo que borre todos sus caballos, incluidos los dados de baja (ruta interna).
  4. Borra la cuenta.
- **Responde:** 204 sin cuerpo.
- **Errores:** 404 cuenta ajena o inexistente; 503 si dieta o registro-caballo no responden (en ese caso la
  cuenta **no** se borra).

## +1. Disponibilidad de correo: `GET /api/usuarios/disponibilidad?correo=ana@correo.com`

- **Acceso:** pública.
- **Qué hace:** indica si ya existe una cuenta con ese correo. No distingue mayúsculas ni espacios.
- **Responde:** 200 (`DisponibilidadResponse`) con el correo normalizado y un mensaje:
  `{ "correo": "ana@correo.com", "mensaje": "Ya existe una cuenta con ese correo" }`, o
  `"No existe una cuenta con ese correo"` si está libre para registrarse.
- **Errores:** 400 si falta el parámetro `correo`.

## +2. Iniciar sesión: `POST /api/auth/login`

- **Acceso:** pública.
- **Recibe** (`LoginRequest`): `{ "correo": "ana@correo.com", "password": "EquinoDietaPrueba7319Xq" }` (los dos obligatorios).
- **Qué hace** (`AutenticacionUseCase.iniciarSesion`):
  1. Busca la cuenta por correo.
  2. Si está bloqueada, responde 423 aunque la contraseña sea correcta.
  3. Si la contraseña es incorrecta, suma un intento fallido. Al tercero bloquea la cuenta 15 minutos (el
     bloqueo se levanta solo cuando pasa ese tiempo).
  4. Si es correcta, reinicia los intentos y emite el JWT (vence en 60 minutos por defecto,
     `app.jwt.expiration-minutes`).
- **Responde:** 200 (`TokenResponse`):

```json
{ "token": "eyJhbGciOi...", "tipo": "Bearer", "expiraEn": "2026-11-16T16:00:00Z",
  "usuario": { "id": 1, "nombre": "Ana Torres", "correo": "ana@correo.com", "...": "..." } }
```

- **Errores:** 400 cuerpo incompleto; 401 correo no registrado o contraseña incorrecta (el tercer fallo avisa
  "La cuenta quedó bloqueada por 15 minutos"); 423 cuenta bloqueada.

## +3. Perfil del usuario autenticado: `GET /api/auth/perfil`

- **Acceso:** token.
- **Qué hace:** devuelve los datos actuales de la cuenta del token, sin tener que conocer el id.
- **Responde:** 200 con el usuario.
- **Errores:** 401 sin token, o si la cuenta del token ya no existe.

## +4. Cambiar contraseña: `PUT /api/usuarios/{id}/password`

- **Acceso:** token. Solo la cuenta propia.
- **Recibe** (`CambioPasswordRequest`): `{ "passwordActual": "EquinoDietaPrueba7319Xq", "passwordNueva": "EquinoDietaNueva8426Zk" }`;
  la nueva de 8 a 72 caracteres y máximo 72 bytes.
- **Qué hace:** comprueba la contraseña actual, exige que la nueva sea distinta y que no aparezca en filtraciones
  (API Pwned Passwords), guarda la nueva encriptada y avisa al usuario por correo.
- **Responde:** 204 sin cuerpo.
- **Errores:** 401 contraseña actual incorrecta; 400 nueva igual a la actual, demasiado corta o filtrada; 404 cuenta
  ajena.

## +5. Renovar token: `POST /api/auth/renovacion`

- **Acceso:** token todavía vigente. No lleva cuerpo.
- **Qué hace:** emite un token nuevo con un vencimiento nuevo, para no tener que volver a escribir la contraseña.
- **Responde:** 200 con el mismo formato que el login.
- **Errores:** 401 sin token, token vencido o cuenta ya eliminada.

---

# registro-caballo (puerto 8082)

Todas las rutas piden token. El caballo siempre queda a nombre del usuario del token.

| Tipo | Método y ruta | Respuesta correcta |
|---|---|---|
| C | `POST /api/caballos` | 201 + caballo |
| R | `GET /api/caballos/{id}` | 200 + caballo |
| R | `GET /api/caballos?page=0&size=10` | 200 + página |
| U | `PUT /api/caballos/{id}` | 200 + caballo |
| D | `DELETE /api/caballos/{id}` | 204 |
| +1 | `POST /api/caballos/{id}/reactivacion` | 200 + caballo |
| +2 | `PATCH /api/caballos/{id}/estado-salud` | 200 + caballo |
| +3 | `PATCH /api/caballos/{id}/peso` | 200 + caballo |
| +4 | `GET /api/caballos/codigo/{codigo}` | 200 + caballo |
| +5 | `GET /api/caballos/busqueda?categoria=&estado=&page=&size=` | 200 + página |

La respuesta "caballo" (`CaballoResponse`):

```json
{ "id": 1, "nombre": "Trueno", "codigo": "EQ-001", "pesoKg": 500.0, "categoria": "adulto",
  "estadoSalud": "laminitis", "activo": true,
  "creadoEn": "2026-11-16T15:00:00Z", "actualizadoEn": "2026-11-16T15:00:00Z" }
```

Valores válidos: `categoria` es `potrillo`, `joven` o `adulto`; `estadoSalud` es `sin_complicaciones`,
`deshidratacion`, `desnutricion`, `laminitis` o `colicos`. Se aceptan en mayúsculas y se guardan en minúsculas.

## C. Registrar caballo: `POST /api/caballos`

- **Recibe** (`CaballoRequest`):

```json
{ "nombre": "Trueno", "codigo": "EQ-001", "pesoKg": 500.0, "categoria": "adulto", "estadoSalud": "laminitis" }
```

  `nombre` obligatorio (máximo 120); `codigo` obligatorio, máximo 40, solo letras, números, guion y guion bajo;
  `pesoKg` de 1 a 2000 con máximo 2 decimales; `categoria` y `estadoSalud` obligatorios.
- **Qué hace:** guarda el código en mayúsculas, revisa que ningún caballo **activo** (de cualquier usuario) lo
  use y crea el caballo activo a nombre del usuario del token.
- **Responde:** 201 con el caballo.
- **Errores:** 400 datos inválidos, categoría o estado que no existen; 409 código ya usado por un caballo activo.

## R. Consultar caballo: `GET /api/caballos/{id}`

- **Qué hace:** devuelve el caballo si es del usuario y está activo.
- **Responde:** 200 con el caballo.
- **Errores:** 404 si no existe, está dado de baja o es de otro usuario; 400 si el id no es un número.

## R. Listar caballos: `GET /api/caballos?page=0&size=10`

- **Qué hace:** lista los caballos activos del usuario, ordenados por nombre, por páginas.
- **Responde:** 200 con una página de caballos.
- **Errores:** 400 si `page` o `size` están fuera de los límites.

## U. Actualizar caballo: `PUT /api/caballos/{id}`

- **Recibe:** el mismo cuerpo que el registro, con todos los datos.
- **Qué hace:** reemplaza nombre, código, peso, categoría y estado. Puede conservar su propio código, pero no
  tomar uno que use otro caballo activo.
- **Responde:** 200 con el caballo actualizado.
- **Errores:** 400 datos inválidos; 404 caballo ajeno o dado de baja; 409 código de otro caballo activo.

## D. Eliminar caballo: `DELETE /api/caballos/{id}`

- **Qué hace:** borrado lógico. El caballo no se borra de la tabla: queda con `activo = false`, deja de aparecer
  en las consultas y libera su código.
- **Responde:** 204 sin cuerpo.
- **Errores:** 404 si no existe, ya estaba dado de baja o es de otro usuario.

## +1. Reactivar caballo: `POST /api/caballos/{id}/reactivacion`

- **Qué hace:** vuelve a activar un caballo dado de baja del usuario. No lleva cuerpo.
- **Responde:** 200 con el caballo, ahora con `activo: true`.
- **Errores:** 404 si no existe o es de otro usuario; 409 si ya está activo o si otro caballo activo tomó su código.

## +2. Cambiar estado de salud: `PATCH /api/caballos/{id}/estado-salud`

- **Recibe** (`EstadoSaludRequest`): `{ "estadoSalud": "colicos" }`.
- **Qué hace:** cambia solo el estado de salud. Es importante porque decide el perfil de dieta que se aplicará.
- **Responde:** 200 con el caballo.
- **Errores:** 400 estado vacío o que no existe; 404 caballo ajeno o dado de baja.

## +3. Actualizar peso: `PATCH /api/caballos/{id}/peso`

- **Recibe** (`PesoRequest`): `{ "pesoKg": 480.5 }` (de 1 a 2000, máximo 2 decimales).
- **Qué hace:** cambia solo el peso, que es la base del cálculo de la ración.
- **Responde:** 200 con el caballo.
- **Errores:** 400 peso fuera de rango o con más de 2 decimales; 404 caballo ajeno o dado de baja.

## +4. Buscar por código: `GET /api/caballos/codigo/{codigo}`

- **Qué hace:** busca entre los caballos activos del usuario por su código, sin importar mayúsculas
  (`eq-001` encuentra `EQ-001`).
- **Responde:** 200 con el caballo.
- **Errores:** 404 si no hay un caballo activo del usuario con ese código.

## +5. Búsqueda filtrada: `GET /api/caballos/busqueda?categoria=adulto&estado=laminitis&page=0&size=10`

- **Qué hace:** lista los caballos activos del usuario filtrando por categoría, por estado o por los dos. Los
  filtros son opcionales: sin filtros devuelve todos. El filtro se aplica en la base de datos.
- **Responde:** 200 con una página de caballos.
- **Errores:** 400 categoría o estado que no existen, o paginación fuera de límites.

## Ruta interna: `DELETE /api/internal/caballos/usuario/{usuarioId}`

- **Acceso:** no usa token; exige el encabezado `X-Internal-Key` con la clave compartida. Solo la llama login.
- **Qué hace:** borra de la tabla todos los caballos de ese usuario, activos o dados de baja. Es el paso 3 del
  borrado en cascada. A diferencia de `DELETE /api/caballos/{id}`, este borrado es físico: si se va el usuario, no
  queda nada suyo.
- **Responde:** 200 con `{ "usuarioId": 1, "caballosEliminados": 2 }`.
- **Errores:** 401 si falta la clave o no coincide ("Encabezado X-Internal-Key ausente o incorrecto").

---

# dieta (puerto 8083)

Todas las rutas piden token. Para generar o regenerar una dieta, dieta consulta el caballo en registro-caballo
(`GET /api/caballos/{id}`) reenviando el mismo token.

| Tipo | Método y ruta | Respuesta correcta |
|---|---|---|
| C | `POST /api/dietas` | 201 + dieta |
| R | `GET /api/dietas/{id}` | 200 + dieta |
| R | `GET /api/dietas?page=0&size=10` | 200 + página |
| U | `PUT /api/dietas/{id}` | 200 + dieta |
| D | `DELETE /api/dietas/{id}` | 204 |
| +1 | `POST /api/dietas/vista-previa` | 200 + cálculo |
| +2 | `POST /api/dietas/{id}/regeneracion` | 201 + dieta nueva |
| +3 | `GET /api/dietas/caballo/{caballoId}/vigente` | 200 + dieta |
| +4 | `GET /api/dietas/caballo/{caballoId}?page=0&size=10` | 200 + página |
| +5 | `GET /api/dietas/perfiles?categoria=&estado=` | 200 + lista de perfiles |

La respuesta "dieta" (`DietaResponse`) trae la copia del caballo, la ración, el perfil completo y el aviso:

```json
{
  "id": 1, "caballoId": 1, "caballoNombre": "Trueno", "caballoCodigo": "EQ-001",
  "categoria": "adulto", "estadoSalud": "laminitis", "pesoKg": 480.5,
  "racion": { "materiaSecaTotalKgDia": 7.21, "forrajeKgDia": 6.85, "concentradoKgDia": 0.36,
              "criterio": "1.5% del peso corporal en MS/día (95% forraje / 5% concentrado)" },
  "perfil": { "id": 13, "categoria": "adulto", "estadoSalud": "laminitis", "inferido": false, "...": "..." },
  "observaciones": "Primera dieta",
  "creadoEn": "2026-11-16T15:00:00Z",
  "aviso": "Recomendación orientativa basada en reglas documentadas del NRC. No es un diagnóstico veterinario..."
}
```

El `perfil` (`PerfilResponse`) incluye: nombre del estado, fuente, criterio nutricional, requerimiento NRC de
referencia, porcentajes, forrajes y concentrados recomendados y a evitar, manejo del agua, suplementos, reglas de
alimentación y advertencias. Si el perfil es *inferido*, el `aviso` agrega una advertencia extra. El `id` del
perfil del ejemplo es ilustrativo: depende del orden de carga del catálogo.

**Cómo se calcula la ración** (`Racion.calcular`, redondeo a 2 decimales):

- materia seca total = peso x % del peso del perfil (480.5 x 1.5 % = 7.21 kg)
- forraje = materia seca x % de forraje (7.21 x 95 % = 6.85 kg)
- concentrado = materia seca menos forraje (7.21 - 6.85 = 0.36 kg)

## C. Generar dieta: `POST /api/dietas`

- **Recibe** (`GenerarDietaRequest`): `{ "caballoId": 1, "observaciones": "Primera dieta" }`. `caballoId`
  obligatorio y positivo; `observaciones` opcional, máximo 2000.
- **Qué hace** (`DietaUseCase.generar`):
  1. Pide el caballo a registro-caballo con el token del usuario.
  2. Elige el perfil del catálogo según la categoría y el estado de salud del caballo.
  3. Calcula la ración con el peso real del caballo.
  4. Guarda la dieta con una copia del nombre, código, categoría, estado y peso del caballo, para que el
     historial no cambie si después cambia el caballo.
  5. Avisa por correo al usuario y agrega al campo `aviso` el clima de la zona de referencia (API Open-Meteo). Si el
     clima no está disponible, la dieta se genera igual, sin ese aviso.
- **Responde:** 201 con la dieta.
- **Errores:** 400 sin `caballoId` u observaciones de más de 2000; 404 caballo inexistente, dado de baja o de
  otro usuario; 503 si registro-caballo no responde.

## R. Consultar dieta: `GET /api/dietas/{id}`

- **Qué hace:** devuelve la dieta si es del usuario, con su perfil del catálogo.
- **Responde:** 200 con la dieta.
- **Errores:** 404 si no existe o es de otro usuario.

## R. Listar dietas: `GET /api/dietas?page=0&size=10`

- **Qué hace:** lista todas las dietas del usuario, de la más reciente a la más antigua.
- **Responde:** 200 con una página de dietas.
- **Errores:** 400 paginación fuera de límites.

## U. Actualizar observaciones: `PUT /api/dietas/{id}`

- **Recibe** (`ObservacionesRequest`): `{ "observaciones": "Revisar condición corporal en 15 días" }`.
- **Qué hace:** cambia **solo** las observaciones; la ración y los datos del caballo no se tocan. Un texto vacío
  deja las observaciones en `null`.
- **Responde:** 200 con la dieta.
- **Errores:** 400 más de 2000 caracteres; 404 dieta ajena o inexistente.

## D. Eliminar dieta: `DELETE /api/dietas/{id}`

- **Qué hace:** borra la dieta de la tabla (aquí el borrado sí es definitivo, no lógico).
- **Responde:** 204 sin cuerpo.
- **Errores:** 404 dieta ajena o inexistente.

## +1. Vista previa: `POST /api/dietas/vista-previa`

- **Recibe** (`VistaPreviaRequest`):
  `{ "categoria": "adulto", "estadoSalud": "laminitis", "pesoKg": 500.0 }` (peso de 1 a 2000, máximo 2 decimales).
- **Qué hace:** calcula la ración y muestra el perfil **sin guardar nada** y sin consultar ningún caballo. Sirve
  para probar combinaciones.
- **Responde:** 200 (`VistaPreviaResponse`) con `categoria`, `estadoSalud`, `pesoKg`, `racion`, `perfil` y
  `aviso`. Con 500 kg adulto con laminitis: 7.5 / 7.12 / 0.38 kg.
- **Errores:** 400 datos inválidos o combinación de categoría y estado sin perfil.

## +2. Regenerar dieta: `POST /api/dietas/{id}/regeneracion`

- **Qué hace:** toma una dieta anterior y crea una **nueva** para el mismo caballo con sus datos **actuales**
  (por ejemplo, si cambió el peso o el estado de salud). Conserva las observaciones. La dieta anterior no se borra.
- **Responde:** 201 con la dieta nueva, que pasa a ser la vigente.
- **Errores:** 404 dieta ajena o inexistente, o caballo que ya no existe; 503 si registro-caballo no responde.

## +3. Dieta vigente: `GET /api/dietas/caballo/{caballoId}/vigente`

- **Qué hace:** devuelve la dieta más reciente de ese caballo para el usuario.
- **Responde:** 200 con la dieta.
- **Errores:** 404 si el usuario no tiene dietas de ese caballo.

## +4. Historial del caballo: `GET /api/dietas/caballo/{caballoId}?page=0&size=10`

- **Qué hace:** lista las dietas de ese caballo, de la más reciente a la más antigua.
- **Responde:** 200 con una página. Si el caballo es de otro usuario, la página llega vacía (no da 404).
- **Errores:** 400 paginación fuera de límites.

## +5. Catálogo de perfiles: `GET /api/dietas/perfiles?categoria=adulto&estado=laminitis`

- **Qué hace:** devuelve los perfiles del catálogo NRC. Sin filtros, los 15; los filtros `categoria` y `estado`
  son opcionales y no distinguen mayúsculas.
- **Responde:** 200 con una lista de perfiles. Si nada coincide, la lista llega vacía (`[]`).
- **Errores:** 401 sin token.

## Ruta interna: `DELETE /api/internal/dietas/usuario/{usuarioId}`

- **Acceso:** no usa token; exige `X-Internal-Key`. Solo la llama login.
- **Qué hace:** borra todas las dietas de ese usuario. Es el paso 2 del borrado en cascada.
- **Responde:** 200 con `{ "usuarioId": 1, "dietasEliminadas": 3 }`.
- **Errores:** 401 si falta la clave o no coincide.

# notificaciones (puerto 8084)

## Ruta interna: `POST /api/internal/notificaciones`

- **Acceso:** encabezado `X-Internal-Key`. Solo la llaman los demás microservicios.
- **Recibe** (`NotificacionRequest`):

```json
{
  "correo": "ana@correo.com",
  "nombre": "Ana Torres",
  "tipo": "DIETA_GENERADA",
  "datos": { "caballo": "Trueno", "codigo": "EQ-001", "msTotalKg": "7.21", "forrajeKg": "6.85", "concentradoKg": "0.36" }
}
```

  `tipo`: `BIENVENIDA`, `CONTRASENA_CAMBIADA`, `CUENTA_ACTUALIZADA`, `DIETA_GENERADA` o `ESTADO_SALUD_CAMBIADO`.
  `datos` es opcional.
- **Qué hace:** arma el correo según el tipo y lo envía en segundo plano con Spring Mail.
- **Responde:** 202 sin cuerpo.
- **Errores:** 400 si los datos no son válidos; 401 sin `X-Internal-Key` o con una clave incorrecta.
- **En Postman:** carpeta `notificaciones` de la colección, con el entorno `DietaEquinos-local`. El destinatario se cambia en la
  variable `correo_notificacion`.
