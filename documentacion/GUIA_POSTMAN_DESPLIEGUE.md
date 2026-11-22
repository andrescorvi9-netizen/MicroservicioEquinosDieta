# Guía de Postman y base de datos del despliegue

Cómo probar a mano los microservicios desplegados en Render con Postman y cómo revisar los datos en Neon.

| Microservicio | URL desplegada | Puerto local |
|---|---|---|
| login | `https://equinos-login.onrender.com` | 8081 |
| registro-caballo | `https://equinos-registrocaballo.onrender.com` | 8082 |
| dieta | `https://equinos-dieta.onrender.com` | 8083 |
| notificaciones | `https://equinos-notificaciones.onrender.com` | 8084 |

---

## 1. Importar en Postman

Los archivos están en `postman/importar` (esta carpeta no se sube a GitHub):

| Archivo | Contenido |
|---|---|
| `DietaEquinos.postman_collection.json` | Colección: en cada microservicio, el CRUD (C, R, U, D) y 5 endpoints extra (1 a 5) |
| `DietaEquinos-render.postman_environment.json` | Variables apuntando a Render |
| `DietaEquinos-local.postman_environment.json` | Variables apuntando a `localhost` |

1. Borrar en Postman la colección y los environments anteriores para no tener duplicados.
2. **Import**, y arrastrar los 3 archivos.
3. En **DietaEquinos-render**, escribir en `internal_key` el mismo valor de `INTERNAL_KEY` configurado en Render.
4. Elegir el environment arriba a la derecha.

---

## 2. Cómo funcionan los environments

Un environment es un conjunto de variables con nombre. Las peticiones no tienen la dirección escrita: usan marcadores como
`{{usuarios_url}}`, y Postman los reemplaza por el valor del environment seleccionado.

```
Petición:                     POST {{usuarios_url}}/api/auth/login
Con DietaEquinos-local:       POST http://localhost:8081/api/auth/login
Con DietaEquinos-render:      POST https://equinos-login.onrender.com/api/auth/login
```

Así, la misma colección sirve para los servicios del PC y para los desplegados, cambiando solo el environment. Al desplegar
solo cambió el host: las rutas `/api/...` son las mismas.

### Variables

| Variable | Local | Render | Uso |
|---|---|---|---|
| `usuarios_url` | `http://localhost:8081` | `https://equinos-login.onrender.com` | login |
| `caballos_url` | `http://localhost:8082` | `https://equinos-registrocaballo.onrender.com` | registro-caballo |
| `dietas_url` | `http://localhost:8083` | `https://equinos-dieta.onrender.com` | dieta |
| `notificaciones_url` | `http://localhost:8084` | `https://equinos-notificaciones.onrender.com` | notificaciones |
| `internal_key` | `clave-interna-de-desarrollo` | la de Render (se escribe a mano) | Encabezado `X-Internal-Key` |
| `token` | se llena solo | se llena solo | JWT del usuario |

`internal_key` y `token` son de tipo *secret*: Postman los muestra ocultos.

### Cómo viaja el token

1. **2 - Iniciar sesión** responde con el JWT.
2. Su script (pestaña *Scripts*) lo guarda en la variable `token`.
3. La colección tiene *Authorization → Bearer Token = `{{token}}`*, así que cada petición envía
   `Authorization: Bearer <token>` sin copiarlo a mano.
4. El token vence en 60 minutos. **5 - Renovar token** entrega uno nuevo y también lo guarda.

Para verlo en vivo durante la exposición, abrir el environment con el ícono del ojo mientras se ejecutan las peticiones.

### URL corta o completa

En la colección se usa la **URL corta** (`{{usuarios_url}}/api/usuarios`). Si se escribe la completa, la petición queda
amarrada a un solo servidor y deja de cambiar con el environment. Al pasar el mouse sobre `{{usuarios_url}}`, Postman muestra
el valor real; si aparece en rojo, no hay environment seleccionado.

### Parámetros de la URL

- **`:id`, `:codigo`, `:caballoId`**: se escriben en *Params → Path Variables*. Por ejemplo,
  `{{caballos_url}}/api/caballos/:id` con `id = 5` envía `/api/caballos/5`.
- **Lo que va después de `?`** (`page`, `size`, `categoria`, `estado`, `correo`): se cambia en *Params → Query Params*.
- `:id` es distinto en cada microservicio: usuario en login, caballo en registro-caballo y dieta en dieta.

---

## 3. URLs de cada endpoint

Cada variable va con su microservicio. Si se mezclan (por ejemplo `{{usuarios_url}}/api/caballos`), la petición llega al
servicio equivocado y responde 404.

### login

| Petición | Método | URL corta | URL completa en Render | Token |
|---|---|---|---|---|
| C - Registrar usuario | POST | `{{usuarios_url}}/api/usuarios` | `https://equinos-login.onrender.com/api/usuarios` | No |
| R - Consultar usuario | GET | `{{usuarios_url}}/api/usuarios/:id` | `https://equinos-login.onrender.com/api/usuarios/{id}` | Sí |
| U - Actualizar usuario | PUT | `{{usuarios_url}}/api/usuarios/:id` | `https://equinos-login.onrender.com/api/usuarios/{id}` | Sí |
| D - Eliminar usuario | DELETE | `{{usuarios_url}}/api/usuarios/:id` | `https://equinos-login.onrender.com/api/usuarios/{id}` | Sí |
| 1 - Consultar disponibilidad de correo | GET | `{{usuarios_url}}/api/usuarios/disponibilidad?correo=...` | `https://equinos-login.onrender.com/api/usuarios/disponibilidad?correo=andrescorvi9@gmail.com` | No |
| 2 - Iniciar sesión | POST | `{{usuarios_url}}/api/auth/login` | `https://equinos-login.onrender.com/api/auth/login` | No |
| 3 - Ver perfil | GET | `{{usuarios_url}}/api/auth/perfil` | `https://equinos-login.onrender.com/api/auth/perfil` | Sí |
| 4 - Cambiar contraseña | PUT | `{{usuarios_url}}/api/usuarios/:id/password` | `https://equinos-login.onrender.com/api/usuarios/{id}/password` | Sí |
| 5 - Renovar token | POST | `{{usuarios_url}}/api/auth/renovacion` | `https://equinos-login.onrender.com/api/auth/renovacion` | Sí |

### registro-caballo (todas con token)

| Petición | Método | URL corta | URL completa en Render |
|---|---|---|---|
| C - Registrar caballo | POST | `{{caballos_url}}/api/caballos` | `https://equinos-registrocaballo.onrender.com/api/caballos` |
| R - Consultar caballo | GET | `{{caballos_url}}/api/caballos/:id` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}` |
| R - Listar caballos | GET | `{{caballos_url}}/api/caballos?page=0&size=10` | `https://equinos-registrocaballo.onrender.com/api/caballos?page=0&size=10` |
| U - Actualizar caballo | PUT | `{{caballos_url}}/api/caballos/:id` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}` |
| D - Eliminar caballo | DELETE | `{{caballos_url}}/api/caballos/:id` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}` |
| 1 - Reactivar caballo | POST | `{{caballos_url}}/api/caballos/:id/reactivacion` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}/reactivacion` |
| 2 - Cambiar estado de salud | PATCH | `{{caballos_url}}/api/caballos/:id/estado-salud` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}/estado-salud` |
| 3 - Actualizar peso | PATCH | `{{caballos_url}}/api/caballos/:id/peso` | `https://equinos-registrocaballo.onrender.com/api/caballos/{id}/peso` |
| 4 - Buscar por código | GET | `{{caballos_url}}/api/caballos/codigo/:codigo` | `https://equinos-registrocaballo.onrender.com/api/caballos/codigo/{codigo}` |
| 5 - Búsqueda filtrada | GET | `{{caballos_url}}/api/caballos/busqueda?categoria=adulto&estado=laminitis&page=0&size=10` | `https://equinos-registrocaballo.onrender.com/api/caballos/busqueda?categoria=adulto&estado=laminitis&page=0&size=10` |

### dieta (todas con token)

| Petición | Método | URL corta | URL completa en Render |
|---|---|---|---|
| C - Generar dieta | POST | `{{dietas_url}}/api/dietas` | `https://equinos-dieta.onrender.com/api/dietas` |
| R - Consultar dieta | GET | `{{dietas_url}}/api/dietas/:id` | `https://equinos-dieta.onrender.com/api/dietas/{id}` |
| R - Listar dietas | GET | `{{dietas_url}}/api/dietas?page=0&size=10` | `https://equinos-dieta.onrender.com/api/dietas?page=0&size=10` |
| U - Actualizar observaciones | PUT | `{{dietas_url}}/api/dietas/:id` | `https://equinos-dieta.onrender.com/api/dietas/{id}` |
| D - Eliminar dieta | DELETE | `{{dietas_url}}/api/dietas/:id` | `https://equinos-dieta.onrender.com/api/dietas/{id}` |
| 1 - Vista previa | POST | `{{dietas_url}}/api/dietas/vista-previa` | `https://equinos-dieta.onrender.com/api/dietas/vista-previa` |
| 2 - Regenerar dieta | POST | `{{dietas_url}}/api/dietas/:id/regeneracion` | `https://equinos-dieta.onrender.com/api/dietas/{id}/regeneracion` |
| 3 - Dieta vigente | GET | `{{dietas_url}}/api/dietas/caballo/:caballoId/vigente` | `https://equinos-dieta.onrender.com/api/dietas/caballo/{caballoId}/vigente` |
| 4 - Historial del caballo | GET | `{{dietas_url}}/api/dietas/caballo/:caballoId?page=0&size=10` | `https://equinos-dieta.onrender.com/api/dietas/caballo/{caballoId}?page=0&size=10` |
| 5 - Catálogo de perfiles | GET | `{{dietas_url}}/api/dietas/perfiles?categoria=adulto&estado=laminitis` | `https://equinos-dieta.onrender.com/api/dietas/perfiles?categoria=adulto&estado=laminitis` |

### notificaciones

| Petición | Método | URL corta | URL completa en Render | Acceso |
|---|---|---|---|---|
| Enviar notificación | POST | `{{notificaciones_url}}/api/internal/notificaciones` | `https://equinos-notificaciones.onrender.com/api/internal/notificaciones` | Encabezado `X-Internal-Key`, sin token |

---

## 4. Cuerpos JSON para probar a mano

En las peticiones con cuerpo: *Body → raw → JSON*. Las que piden token lo toman de la colección; las marcadas "sin token"
tienen *Authorization → No Auth*.

### login

**C - Registrar usuario** · `POST /api/usuarios` · sin token · envía correo de bienvenida

```json
{
  "nombre": "Andres Corvi",
  "correo": "andrescorvi9@gmail.com",
  "password": "EquinoDietaPrueba7319Xq"
}
```

Anotar el `id` de la respuesta. La contraseña debe tener de 8 a 72 caracteres y no aparecer en filtraciones conocidas
(`12345678` responde 400). Un correo solo se puede registrar una vez (409).

**2 - Iniciar sesión** · `POST /api/auth/login` · sin token

```json
{
  "correo": "andrescorvi9@gmail.com",
  "password": "EquinoDietaPrueba7319Xq"
}
```

El token se guarda solo en la variable `token`.

**R - Consultar usuario** · `GET /api/usuarios/:id` · sin cuerpo

**U - Actualizar usuario** · `PUT /api/usuarios/:id` · envía correo si cambia el nombre o el correo

```json
{
  "nombre": "Andres Corvi Actualizado",
  "correo": "andrescorvi9@gmail.com"
}
```

Si se envían los mismos datos que ya tiene la cuenta, responde 200 pero no envía correo. Si cambia el correo, el aviso
llega a la dirección nueva y a la anterior.

**1 - Consultar disponibilidad de correo** · `GET /api/usuarios/disponibilidad?correo=andrescorvi9@gmail.com` · sin cuerpo,
sin token. Responde `"mensaje": "Ya existe una cuenta con ese correo"` o `"No existe una cuenta con ese correo"`.
Si el correo tiene `+`, se escribe `%2B`.

**3 - Ver perfil** · `GET /api/auth/perfil` · sin cuerpo

**4 - Cambiar contraseña** · `PUT /api/usuarios/:id/password` · envía correo

```json
{
  "passwordActual": "EquinoDietaPrueba7319Xq",
  "passwordNueva": "EquinoDietaNueva8426Zk"
}
```

Desde aquí el login se hace con `EquinoDietaNueva8426Zk`.

**5 - Renovar token** · `POST /api/auth/renovacion` · sin cuerpo

**D - Eliminar usuario** · `DELETE /api/usuarios/:id` · sin cuerpo. Borra en cascada las dietas, los caballos y la
cuenta. Ejecutar al final de la demostración.

### registro-caballo

**C - Registrar caballo** · `POST /api/caballos`

```json
{
  "nombre": "Trueno",
  "codigo": "EQ-001",
  "pesoKg": 500.0,
  "categoria": "adulto",
  "estadoSalud": "sin_complicaciones"
}
```

Anotar el `id`. Si el código ya lo usa un caballo activo responde 409: cambiarlo (por ejemplo `EQ-002`).

**R - Consultar caballo** · `GET /api/caballos/:id` · sin cuerpo

**R - Listar caballos** · `GET /api/caballos?page=0&size=10` · sin cuerpo

**U - Actualizar caballo** · `PUT /api/caballos/:id`

```json
{
  "nombre": "Trueno II",
  "codigo": "EQ-001",
  "pesoKg": 510.0,
  "categoria": "adulto",
  "estadoSalud": "sin_complicaciones"
}
```

**D - Eliminar caballo** · `DELETE /api/caballos/:id` · sin cuerpo. Borrado lógico (`activo = false`).

**1 - Reactivar caballo** · `POST /api/caballos/:id/reactivacion` · sin cuerpo. Recupera el caballo borrado en D.

**2 - Cambiar estado de salud** · `PATCH /api/caballos/:id/estado-salud` · envía correo

```json
{
  "estadoSalud": "laminitis"
}
```

**3 - Actualizar peso** · `PATCH /api/caballos/:id/peso`

```json
{
  "pesoKg": 480.5
}
```

**4 - Buscar por código** · `GET /api/caballos/codigo/eq-001` · sin cuerpo (no distingue mayúsculas)

**5 - Búsqueda filtrada** · `GET /api/caballos/busqueda?categoria=adulto&estado=laminitis&page=0&size=10` · sin cuerpo

Valores válidos: `categoria` = `potrillo`, `joven`, `adulto`; `estadoSalud` = `sin_complicaciones`, `deshidratacion`,
`desnutricion`, `laminitis`, `colicos`. Peso de 1 a 2000 kg con máximo 2 decimales.

### dieta

**C - Generar dieta** · `POST /api/dietas` · envía correo

```json
{
  "caballoId": 1,
  "observaciones": "Primera dieta"
}
```

Cambiar `1` por el id real del caballo y anotar el `id` de la dieta.

**R - Consultar dieta** · `GET /api/dietas/:id` · sin cuerpo

**R - Listar dietas** · `GET /api/dietas?page=0&size=10` · sin cuerpo

**U - Actualizar observaciones** · `PUT /api/dietas/:id`

```json
{
  "observaciones": "Revisar condicion corporal en 15 dias"
}
```

**1 - Vista previa** · `POST /api/dietas/vista-previa` · no guarda nada

```json
{
  "categoria": "adulto",
  "estadoSalud": "laminitis",
  "pesoKg": 500.0
}
```

Resultado esperado: materia seca 7.5 kg, forraje 7.12 kg, concentrado 0.38 kg.

**2 - Regenerar dieta** · `POST /api/dietas/:id/regeneracion` · sin cuerpo · crea una dieta nueva y envía correo

**3 - Dieta vigente** · `GET /api/dietas/caballo/:caballoId/vigente` · sin cuerpo

**4 - Historial del caballo** · `GET /api/dietas/caballo/:caballoId?page=0&size=10` · sin cuerpo

**5 - Catálogo de perfiles** · `GET /api/dietas/perfiles?categoria=adulto&estado=laminitis` · sin cuerpo

**D - Eliminar dieta** · `DELETE /api/dietas/:id` · sin cuerpo. El borrado es definitivo: hacerlo después de 2, 3 y 4, o
usar el id de la dieta regenerada.

### notificaciones

**Enviar notificación** · `POST /api/internal/notificaciones` · sin token, con el encabezado
`X-Internal-Key: {{internal_key}}` · responde 202 sin cuerpo

```json
{
  "correo": "andrescorvi9@gmail.com",
  "nombre": "Andres Corvi",
  "tipo": "BIENVENIDA"
}
```

Con datos, para `DIETA_GENERADA`:

```json
{
  "correo": "andrescorvi9@gmail.com",
  "nombre": "Andres Corvi",
  "tipo": "DIETA_GENERADA",
  "datos": {
    "caballo": "Trueno",
    "codigo": "EQ-001",
    "msTotalKg": "7.5",
    "forrajeKg": "7.12",
    "concentradoKg": "0.38"
  }
}
```

Otros tipos: `CONTRASENA_CAMBIADA` (sin datos), `CUENTA_ACTUALIZADA`
(`"datos": { "nombre": "Andres Corvi", "correo": "andrescorvi9@gmail.com" }`, y `"correoAnterior"` si cambió) y `ESTADO_SALUD_CAMBIADO`
(`"datos": { "caballo": "Trueno", "codigo": "EQ-001", "estadoSalud": "laminitis" }`).

---

## 5. Orden recomendado para la demostración

1. **login:** C → 2 → R → U → 1 → 3 → 4 → 5
2. **registro-caballo:** C → R → R → U → D → 1 → 2 → 3 → 4 → 5
3. **dieta:** C → R → R → U → 1 → 2 → 3 → 4 → 5 → D
4. **notificaciones** (opcional)
5. **login D** al final, para mostrar el borrado en cascada.

Correos que llegan durante la demostración: bienvenida (login C), cuenta actualizada (login U), contraseña cambiada
(login 4), estado de salud (registro-caballo 2) y dieta generada (dieta C y 2).

### Antes de exponer

- Los servicios en plan de pago no se duermen. Si alguno está en plan gratis, se duerme tras 15 minutos sin uso y la primera
  petición tarda alrededor de un minuto: enviar una petición a cada uno unos minutos antes.
- Si la red del PC falla al resolver el nombre del servidor (*Could not resolve host*), volver a enviar la petición.

### Errores frecuentes

| Respuesta | Causa probable |
|---|---|
| 401 en todas las peticiones | Falta iniciar sesión o venció el token (60 minutos): usar 2 o 5 de login |
| 401 en notificaciones | `internal_key` vacía o distinta de la de Render |
| 404 | El `:id` no existe, es de otro usuario, o la variable de la URL es de otro microservicio |
| 409 | Correo o código de caballo repetido |
| 423 | Cuenta bloqueada 15 minutos por 3 intentos fallidos de login |
| URL en rojo en Postman | No hay environment seleccionado |

---

## 6. Ver la base de datos en Neon

Los tres servicios con base de datos comparten la misma base `neondb`, cada uno en su propio esquema:

| Esquema | Dueño | Tablas |
|---|---|---|
| `auth` | login | `usuario` |
| `caballos` | registro-caballo | `caballo` |
| `dietas` | dieta | `dieta` y el catálogo: `categoria`, `estado_salud`, `perfil_dieta`, `perfil_dieta_item` |

Cada esquema tiene además `flyway_schema_history`, donde Flyway registra las migraciones aplicadas.

### Dónde ejecutar las consultas

1. Entrar a [console.neon.tech](https://console.neon.tech) y abrir el proyecto.
2. Menú **SQL Editor**, base de datos **neondb**.
3. Pegar la consulta y pulsar **Run**.

También se puede conectar pgAdmin o DBeaver con host `ep-spring-dew-arlpjs1k.c-4.us-west-2.aws.neon.tech`, puerto `5432`,
base `neondb`, usuario `neondb_owner`, la contraseña de Neon y SSL `require`.

Estas consultas solo leen datos. Para crear, cambiar o borrar, usar la API desde Postman: así se respetan las reglas de
negocio, el borrado en cascada y los correos.

### Usuarios (sin la contraseña encriptada)

```sql
SELECT id, nombre, correo, intentos_fallidos, bloqueado_hasta, creado_en, actualizado_en
FROM auth.usuario
ORDER BY id;
```

### Caballos con el correo de su dueño

```sql
SELECT c.id, c.nombre, c.codigo, c.peso_kg, c.categoria, c.estado_salud, c.activo,
       c.usuario_id, u.correo AS dueno, c.creado_en
FROM caballos.caballo c
LEFT JOIN auth.usuario u ON u.id = c.usuario_id
ORDER BY c.id;
```

Los caballos con `activo = false` son los borrados lógicamente. Si `dueno` sale vacío, el usuario ya fue eliminado.

### Dietas generadas

```sql
SELECT d.id, d.caballo_id, d.caballo_nombre, d.caballo_codigo, d.categoria, d.estado_salud,
       d.peso_kg, d.ms_total_kg, d.forraje_kg, d.concentrado_kg, d.observaciones,
       u.correo AS dueno, d.creado_en
FROM dietas.dieta d
LEFT JOIN auth.usuario u ON u.id = d.usuario_id
ORDER BY d.creado_en DESC;
```

### Todo lo de un usuario por su correo

```sql
SELECT u.id AS usuario, u.correo,
       c.id AS caballo, c.nombre AS caballo_nombre, c.codigo, c.activo,
       d.id AS dieta, d.ms_total_kg, d.forraje_kg, d.concentrado_kg, d.creado_en AS dieta_creada
FROM auth.usuario u
LEFT JOIN caballos.caballo c ON c.usuario_id = u.id
LEFT JOIN dietas.dieta d ON d.caballo_id = c.id
WHERE u.correo = 'andrescorvi9@gmail.com'
ORDER BY c.id, d.creado_en DESC;
```

### Resumen de cantidades

```sql
SELECT 'usuarios' AS tabla, count(*) FROM auth.usuario
UNION ALL SELECT 'caballos activos', count(*) FROM caballos.caballo WHERE activo
UNION ALL SELECT 'caballos dados de baja', count(*) FROM caballos.caballo WHERE NOT activo
UNION ALL SELECT 'dietas', count(*) FROM dietas.dieta
UNION ALL SELECT 'perfiles del catalogo', count(*) FROM dietas.perfil_dieta;
```

Después de arrancar los servicios por primera vez debe haber 15 perfiles en el catálogo.

### Catálogo de perfiles

```sql
SELECT p.id, p.categoria_codigo, p.estado_codigo, p.fuente, p.pct_ms_peso, p.pct_forraje, p.pct_concentrado
FROM dietas.perfil_dieta p
ORDER BY p.categoria_codigo, p.estado_codigo;
```

Recomendaciones de un perfil (forrajes, concentrados, suplementos, reglas y advertencias):

```sql
SELECT i.tipo, i.orden, i.texto
FROM dietas.perfil_dieta_item i
JOIN dietas.perfil_dieta p ON p.id = i.perfil_id
WHERE p.categoria_codigo = 'adulto' AND p.estado_codigo = 'laminitis'
ORDER BY i.tipo, i.orden;
```

### Esquemas, tablas y migraciones

```sql
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema IN ('auth', 'caballos', 'dietas')
ORDER BY table_schema, table_name;
```

```sql
SELECT 'auth' AS esquema, version, description, installed_on, success FROM auth.flyway_schema_history
UNION ALL SELECT 'caballos', version, description, installed_on, success FROM caballos.flyway_schema_history
UNION ALL SELECT 'dietas', version, description, installed_on, success FROM dietas.flyway_schema_history
ORDER BY esquema, installed_on;
```

### Cuentas bloqueadas por intentos fallidos

```sql
SELECT id, correo, intentos_fallidos, bloqueado_hasta
FROM auth.usuario
WHERE bloqueado_hasta > now();
```
