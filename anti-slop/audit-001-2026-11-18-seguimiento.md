# Seguimiento de la auditoría antislop 001 (2026-11-18)

**Resultado:** 11 hallazgos corregidos por completo y 1 ajustado para no tocar la configuración de Gmail (el 12).

## Hallazgos

| # | Estado | Qué se hizo |
|---|---|---|
| 1 | Corregido | `InternalKeyFilter` usa `getServletPath()` en dieta, registro-caballo y notificaciones. Sus pruebas se actualizaron |
| 2 | Corregido | `UsuarioUseCase` responde 400 si la contraseña supera 72 bytes. Se agregaron 2 pruebas |
| 3 | Corregido | El comentario de `DietaUseCase` ahora dice que se consulta la base de datos |
| 4 | Corregido | La guía de estudio tiene las cifras actuales y las clases de prueba que faltaban en sus tablas |
| 5 | Corregido | Se quitaron las 3 rayas largas del catálogo |
| 6 | Corregido | Se borró `postman/collections/Pruebas/` |
| 7 | Corregido | Las 7 excepciones de login tienen una descripción completa |
| 8 | Corregido | `GlobalExceptionHandler` de login usa los nombres cortos |
| 9 | Corregido | Se quitó la variable innecesaria de `CaballoUseCase.registrar` |
| 10 | Corregido | Se simplificó el comentario de `PerfilDietaDataJpaRepository` |
| 11 | Corregido | `app.notificaciones.url` quedó en "Otros microservicios" |
| 12 | Ajustado | La cuenta sigue igual. Solo se agregó un comentario en la plantilla que explica por qué aparece |

## Evidencia

| Servicio | Pruebas | Fallos | Líneas cubiertas |
|---|---|---|---|
| login | 117 | 0 | 98.8% (331 de 335) |
| registro-caballo | 100 | 0 | 99.7% (299 de 300) |
| dieta | 145 | 0 | 99.4% (510 de 513) |
| notificaciones | 25 | 0 | 98.9% (88 de 89) |

La cobertura se midió con el agente de JaCoCo desde fuera del proyecto, sin cambiar los `pom`. IntelliJ puede mostrar valores un poco distintos.

Prueba real del hallazgo 1 con notificaciones corriendo en local y el envío de correo desactivado:

| Petición | Antes | Ahora |
|---|---|---|
| Ruta normal sin clave | 401 | 401 |
| Ruta codificada sin clave | 202 | 401 |
| Ruta normal con clave | 202 | 202 |
| Ruta codificada con clave | No probada | 202 |

## Pendiente de comprobar

- **Rutas internas de dieta y registro-caballo:** el cambio es el mismo y sus pruebas unitarias pasan, pero no se arrancaron para no ejecutar Flyway sobre la base de datos local.
- **Contraseñas largas por HTTP:** el rechazo está cubierto con pruebas unitarias, no con una petición real a login.
- **Catálogo ya cargado:** si la base de datos ya tiene el catálogo, conserva el texto anterior con la raya larga. Solo cambia al cargarlo en una tabla vacía.

## Delivery Gate

Las reglas visuales no aplican porque el proyecto no tiene interfaz.

| Regla | Estado | Evidencia |
|---|---|---|
| R-02 | PASS | No quedan rayas largas en el catálogo |
| R-17 | PASS | Las cifras de la guía salen de la ejecución de hoy |
| R-27 | PASS | La contraseña demasiado larga responde con un error de negocio 400 |
| R-31 | PASS | Hallazgos 7 a 12 resueltos |
| R-35 | PASS | 387 pruebas sin fallos y prueba real en notificaciones |
| R-36 | PASS | La ruta codificada sin clave ya responde 401 en notificaciones |
| R-38 | PASS | Comentario corregido y colección ajena eliminada |
