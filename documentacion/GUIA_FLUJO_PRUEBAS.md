# Guía del flujo del código a través de las pruebas unitarias

Esta guía muestra qué código se ejecuta en cada una de las **382 pruebas unitarias** del proyecto. Sirve para
estudiar el código siguiendo las pruebas: abres la prueba, buscas su fila aquí y vas a las líneas que se indican.

## Cómo leer esta guía

### Camino feliz y camino triste

| Camino | Qué significa | Ejemplo |
|---|---|---|
| **Feliz** | Todo sale bien: los datos son válidos y la operación se completa. | Registrar un usuario con datos correctos. |
| **Triste** | Algo falla o falta: un error, un rechazo, un dato ausente o un servicio que no responde. | Registrar un correo que ya existe. |

### El recorrido

Cada fila tiene el **recorrido**: los métodos del proyecto que se ejecutan, en el orden en que se llaman, con sus
líneas entre paréntesis.

- `Clase.metodo (53)`: se ejecutó la línea 53 de ese método.
- `new Clase (26-30)`: se creó un objeto y se ejecutó su constructor, líneas 26 a 30.
- `(69, sale con excepción)`: el método empezó en la línea 69 y salió por una excepción antes de terminar.
- `Arrange:` el paso ocurre al preparar la prueba, antes de llamar al código que se prueba.

Las líneas de un método se escriben juntas. Si en medio llama a otro método, ese otro aparece como el paso
siguiente. Por ejemplo, en `Registrar` la línea 39 de `registrar` llama a `Usuario.normalizarCorreo` y después
`registrar` sigue en las líneas 40 a 48.

### Qué no aparece en el recorrido

- **Los mocks.** Un mock es un objeto simulado con Mockito: responde lo que la prueba le indica, pero su código real
  no se ejecuta. Por eso un gateway simulado no tiene líneas.
- **El código que genera Java o Lombok.** Por ejemplo, los getters de las entidades o los métodos para leer los campos
  de un `record`.
- **La clase padre de las excepciones.** Cada excepción de negocio también ejecuta el constructor de `DominioException`
  (líneas 7 y 8). Para no repetirlo, no se escribe en cada fila.
- **La línea que llama a un método que lanza una excepción.** Por ejemplo, si un mock lanza un error en la línea 72, esa
  línea no se marca, aunque sí se ejecutó. Las líneas con `throw new ...` sí aparecen.

### Ejemplo explicado: `ObtenerCuentaPropia`

La prueba está en `UsuarioUseCaseTest`. Su fila dice:

> Feliz · `UsuarioUseCase.obtener` (53) → `UsuarioUseCase.cuentaPropia` (119, 122)

En `UsuarioUseCase.java`:

```java
52    public Usuario obtener(Long id, Long solicitanteId) {
53        return cuentaPropia(id, solicitanteId);
...
112   private Usuario cuentaPropia(Long id, Long solicitanteId) {
113       if (id == null || !id.equals(solicitanteId)) {
114           throw new RecursoNoEncontradoException(NO_ENCONTRADO);
115       }
116       return usuarios.buscarPorId(id).orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADO));
```

1. La prueba llama a `obtener(1, 1)`. Se ejecuta la línea 53, que llama a `cuentaPropia`.
2. En la línea 113 el id es igual al del solicitante, así que la condición es falsa y se salta la línea 114.
3. La línea 116 pide el usuario al gateway. Es un mock, así que devuelve el usuario simulado y la prueba termina bien.

En la prueba triste `CuentaAjena` los ids son distintos: la línea 113 es verdadera y se ejecuta la 114, que lanza
la excepción.

### Cómo se obtuvo esta información

No se escribió a mano. Se ejecutaron todas las pruebas con dos herramientas temporales, sin cambiar el proyecto:

- **JaCoCo** marcó las líneas ejecutadas por cada prueba.
- **Un agente de Java** anotó el orden en que se llamó cada método.

Los números de línea corresponden al código del 19 de noviembre de 2026. Si se edita una clase, sus números cambian.

## Resumen

| Servicio | Pruebas | Feliz | Triste |
|---|---|---|---|
| [login](#login) | 118 | 62 | 56 |
| [registro-caballo](#registro-caballo) | 96 | 55 | 41 |
| [dieta](#dieta) | 138 | 76 | 62 |
| [notificaciones](#notificaciones) | 30 | 20 | 10 |
| **Total** | **382** | **213** | **169** |

# login

## Modelo (login)

### [UsuarioTest](../login/src/test/java/com/equinos/login/domain/model/UsuarioTest.java)

Reglas del usuario: crear, bloquear tras 3 intentos y cambiar datos. El usuario es real porque es la clase que se prueba.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarDatosLimpiaNombreYCorreo` | Feliz | `new Usuario` (26-35) → `Usuario.actualizarDatos` (69-72) → `Usuario.normalizarCorreo` (43) |
| `cambiarPasswordGuardaElHashNuevo` | Feliz | `new Usuario` (26-35) → `Usuario.cambiarPassword` (75-77) |
| `elTercerIntentoFallidoBloqueaQuinceMinutos` | Triste | `new Usuario` (26-35) → `Usuario.registrarIntentoFallido` (52-57, 59) |
| `estaBloqueadoSoloMientrasLaFechaNoHayaPasado` | Feliz | `new Usuario` (26-35) → `Usuario.estaBloqueado` (47) |
| `losPrimerosIntentosFallidosSoloSeCuentan` | Triste | `new Usuario` (26-35) → `Usuario.registrarIntentoFallido` (52-54, 59) |
| `normalizarCorreoConvierteNuloEnVacio` | Triste | `Usuario.normalizarCorreo` (43) |
| `nuevoLimpiaLosDatosYEmpiezaSinIntentosNiBloqueo` | Feliz | `Usuario.nuevo` (38) → `Usuario.normalizarCorreo` (43) → `new Usuario` (26-35) |
| `sinFechaDeBloqueoNoEstaBloqueado` | Feliz | `new Usuario` (26-35) → `Usuario.estaBloqueado` (47) |
| `unIngresoExitosoReiniciaIntentosYBloqueo` | Feliz | `new Usuario` (26-35) → `Usuario.registrarIngresoExitoso` (63-66) |

## Casos de uso (login)

### [AutenticacionUseCaseTest](../login/src/test/java/com/equinos/login/domain/usecase/AutenticacionUseCaseTest.java)

Inicio de sesión, renovación del token y perfil.

| Prueba | Camino | Recorrido |
|---|---|---|
| `BloqueadoNoIngresa` | Triste | `AutenticacionUseCase.iniciarSesion` (26-28, 30-31) → `Usuario.normalizarCorreo` (43) → `new CuentaBloqueadaException` (11-13) → `CuentaBloqueadaException.getBloqueadoHasta` (16) |
| `BloqueoVencido` | Feliz | `AutenticacionUseCase.iniciarSesion` (26-28, 30, 34, 44-46) → `Usuario.normalizarCorreo` (43) → `new SesionIniciada` (4) |
| `ContracenaInvalidaIntento` | Triste | `AutenticacionUseCase.iniciarSesion` (26-28, 30, 34-37, 41) → `Usuario.normalizarCorreo` (43) → `new CredencialesInvalidasException` (7-8) |
| `CorreoInexistenteInvalido` | Triste | `AutenticacionUseCase.iniciarSesion` (26-28) → `Usuario.normalizarCorreo` (43) → `new CredencialesInvalidasException` (7-8) |
| `CredencialesCorrectasToken` | Feliz | `AutenticacionUseCase.iniciarSesion` (26-28, 30, 34, 44-46) → `Usuario.normalizarCorreo` (43) → `new SesionIniciada` (4) |
| `RenovarTokenNuevo` | Feliz | `AutenticacionUseCase.renovar` (50-51) → `AutenticacionUseCase.perfil` (55-56) → `new SesionIniciada` (4) |
| `TercerIntentoFallidoBloqueo` | Triste | `AutenticacionUseCase.iniciarSesion` (26-28, 30, 34-39) → `Usuario.normalizarCorreo` (43) → `new CredencialesInvalidasException` (7-8) |
| `perfilDevuelveLaCuentaDelToken` | Feliz | `AutenticacionUseCase.perfil` (55-56) |
| `siLaCuentaDelTokenYaNoExisteSeRechaza` | Triste | `AutenticacionUseCase.perfil` (55-56) → `new CredencialesInvalidasException` (7-8) → `AutenticacionUseCase.renovar` (50, sale con excepción) |

### [UsuarioUseCaseTest](../login/src/test/java/com/equinos/login/domain/usecase/UsuarioUseCaseTest.java)

Registro, consulta, actualización, eliminación en cascada, cambio de contraseña y disponibilidad de correo.

| Prueba | Camino | Recorrido |
|---|---|---|
| `ActualizarConservandoCorreo` | Feliz | `UsuarioUseCase.actualizar` (58-60, 63-67, 70) → `UsuarioUseCase.cuentaPropia` (119, 122) → `Usuario.normalizarCorreo` (43) |
| `ActualizarCorreoConflicto` | Triste | `UsuarioUseCase.actualizar` (58-61) → `UsuarioUseCase.cuentaPropia` (119, 122) → `Usuario.normalizarCorreo` (43) → `new ConflictoException` (7-8) |
| `ActualizarNombreYCorreo` | Feliz | `UsuarioUseCase.actualizar` (58-60, 63-67, 70) → `UsuarioUseCase.cuentaPropia` (119, 122) → `Usuario.normalizarCorreo` (43) |
| `CambiarPassword` | Feliz | `UsuarioUseCase.cambiarPassword` (85-86, 89, 92-97) → `UsuarioUseCase.cuentaPropia` (119, 122) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113, 116) |
| `CambiarPasswordAvisaAlUsuario` | Feliz | `UsuarioUseCase.cambiarPassword` (85-86, 89, 92-97) → `UsuarioUseCase.cuentaPropia` (119, 122) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113, 116) |
| `CambiarPasswordDeMasDe72BytesRechazado` | Triste | `UsuarioUseCase.cambiarPassword` (85) → `UsuarioUseCase.cuentaPropia` (119, 122) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107-108) → `new ReglaNegocioException` (7-8) |
| `CambiarPasswordFiltradaRechazado` | Triste | `UsuarioUseCase.cambiarPassword` (85-86, 89, 92) → `UsuarioUseCase.cuentaPropia` (119, 122) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113-114) → `new ReglaNegocioException` (7-8) |
| `CuentaAjena` | Triste | `UsuarioUseCase.obtener` (53, sale con excepción) → `UsuarioUseCase.cuentaPropia` (119-120) → `new RecursoNoEncontradoException` (7-8) |
| `CuentaNoExisteNoEncontrado` | Triste | `UsuarioUseCase.obtener` (53, sale con excepción) → `UsuarioUseCase.cuentaPropia` (122) → `new RecursoNoEncontradoException` (7-8) |
| `EliminarDietasCaballosCuenta` | Feliz | `UsuarioUseCase.eliminar` (75, 78-81) → `UsuarioUseCase.esCuentaPropiaExistente` (127) |
| `EliminarUnaCuentaAjenaNo` | Triste | `UsuarioUseCase.eliminar` (75-76) → `UsuarioUseCase.esCuentaPropiaExistente` (127) → `new RecursoNoEncontradoException` (7-8) |
| `ObtenerCuentaPropia` | Feliz | `UsuarioUseCase.obtener` (53) → `UsuarioUseCase.cuentaPropia` (119, 122) |
| `Registrar` | Feliz | `UsuarioUseCase.registrar` (39-40, 43-48) → `Usuario.normalizarCorreo` (43) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113, 116) → `Usuario.nuevo` (38) → `new Usuario` (26-35) |
| `RegistrarConPasswordDeMasDe72BytesRechazado` | Triste | `UsuarioUseCase.registrar` (39) → `Usuario.normalizarCorreo` (43) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107-108) → `new ReglaNegocioException` (7-8) |
| `RegistrarConPasswordFiltradaRechazado` | Triste | `UsuarioUseCase.registrar` (39-40, 43) → `Usuario.normalizarCorreo` (43) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113-114) → `new ReglaNegocioException` (7-8) |
| `RegistrarCorreoRepetido` | Triste | `UsuarioUseCase.registrar` (39-41) → `Usuario.normalizarCorreo` (43) → `new ConflictoException` (7-8) |
| `RegistrarEnviaLaBienvenida` | Feliz | `UsuarioUseCase.registrar` (39-40, 43-48) → `Usuario.normalizarCorreo` (43) → `UsuarioUseCase.rechazarSiEsMuyLarga` (107, 110) → `UsuarioUseCase.rechazarSiEstaFiltrada` (113, 116) → `Usuario.nuevo` (38) → `new Usuario` (26-35) |
| `cambiarPasswordConLaActualRechazado` | Triste | `UsuarioUseCase.cambiarPassword` (85-87) → `UsuarioUseCase.cuentaPropia` (119, 122) → `new CredencialesInvalidasException` (7-8) |
| `eliminarUnaCuentaInexistenteNoLlamaALosOtrosServicios` | Triste | `UsuarioUseCase.eliminar` (75-76) → `UsuarioUseCase.esCuentaPropiaExistente` (127) → `new RecursoNoEncontradoException` (7-8) |
| `laContrasenaNuevaDebeSerDistintaDeLaActual` | Triste | `UsuarioUseCase.cambiarPassword` (85-86, 89-90) → `UsuarioUseCase.cuentaPropia` (119, 122) → `new ReglaNegocioException` (7-8) |
| `siDietaNoRespondeNoSeTocanLosCaballosNiLaCuenta` | Triste | Arrange: `new ServicioNoDisponibleException` (7-8) → `UsuarioUseCase.eliminar` (69, sale con excepción) → `UsuarioUseCase.esCuentaPropiaExistente` (127) |
| `siRegistroCaballoNoRespondeLaCuentaNoSeElimina` | Triste | Arrange: `new ServicioNoDisponibleException` (7-8) → `UsuarioUseCase.eliminar` (75, 78) → `UsuarioUseCase.esCuentaPropiaExistente` (127) |
| `correoDisponibleCuandoNoExisteUnaCuentaConEseCorreo` | Feliz | `UsuarioUseCase.correoDisponible` (101) → `Usuario.normalizarCorreo` (43) |
| `correoNoDisponibleCuandoYaExisteUnaCuenta` | Feliz | `UsuarioUseCase.correoDisponible` (101) → `Usuario.normalizarCorreo` (43) |
| `ActualizarConCambiosAvisaAlUsuario` | Feliz | `UsuarioUseCase.actualizar` (58-60, 63-68, 70) → `UsuarioUseCase.cuentaPropia` (119, 122) → `Usuario.normalizarCorreo` (43) |
| `ActualizarSinCambiosNoAvisa` | Feliz | `UsuarioUseCase.actualizar` (58-60, 63-67, 70) → `UsuarioUseCase.cuentaPropia` (119, 122) → `Usuario.normalizarCorreo` (43) |

## Controladores y respuestas (login)

### [AuthControllerTest](../login/src/test/java/com/equinos/login/infraestructure/entry_points/AuthControllerTest.java)

Endpoints de autenticación: login, renovación y perfil.

| Prueba | Camino | Recorrido |
|---|---|---|
| `loginConCredencialesInvalidasDejaPasarLaExcepcion` | Triste | Arrange: `new CredencialesInvalidasException` (7-8) → `AuthController.login` (26, sale con excepción) |
| `loginDevuelveElTokenYLosDatosDelUsuario` | Feliz | `AuthController.login` (26) → `TokenResponse.de` (10-11) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) → `new TokenResponse` (7) |
| `loginEnviaLosDatosDeLaPeticionAlCasoDeUso` | Feliz | Arrange: `new LoginRequest` (5) → `AuthController.login` (26) → `TokenResponse.de` (10-11) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) → `new TokenResponse` (7) |
| `perfilDevuelveLosDatosDelUsuarioAutenticado` | Feliz | `AuthController.perfil` (38) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) |
| `renovarUsaElIdDelUsuarioAutenticado` | Feliz | `AuthController.renovar` (32) → `TokenResponse.de` (10-11) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) → `new TokenResponse` (7) |

### [GlobalExceptionHandlerTest](../login/src/test/java/com/equinos/login/infraestructure/entry_points/GlobalExceptionHandlerTest.java)

Convierte cada excepción en una respuesta HTTP con su código.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conflictoResponde409` | Triste | `new GlobalExceptionHandler` (26) → `new ConflictoException` (7-8) → `GlobalExceptionHandler.dominio` (32) → `GlobalExceptionHandler.estadoDe` (83, 86-87) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `credencialesInvalidasResponde401` | Triste | `new GlobalExceptionHandler` (26) → `new CredencialesInvalidasException` (7-8) → `GlobalExceptionHandler.credenciales` (37) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `cuentaBloqueadaResponde423` | Triste | `new GlobalExceptionHandler` (26) → `new CuentaBloqueadaException` (11-13) → `GlobalExceptionHandler.cuentaBloqueada` (42) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `cuerpoIlegibleResponde400` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.cuerpoIlegible` (56) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `lasExcepcionesDeSpringConservanSuCodigo` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.inesperado` (74-76) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `parametroDeTipoInvalidoIndicaSuNombre` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.tipoInvalido` (61-62) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `recursoNoEncontradoResponde404` | Triste | `new GlobalExceptionHandler` (26) → `new RecursoNoEncontradoException` (7-8) → `GlobalExceptionHandler.dominio` (32) → `GlobalExceptionHandler.estadoDe` (83-84) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `reglaDeNegocioResponde400` | Triste | `new GlobalExceptionHandler` (26) → `new ReglaNegocioException` (7-8) → `GlobalExceptionHandler.dominio` (32) → `GlobalExceptionHandler.estadoDe` (83, 86, 89, 92) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `servicioNoDisponibleResponde503` | Triste | `new GlobalExceptionHandler` (26) → `new ServicioNoDisponibleException` (7-8) → `GlobalExceptionHandler.dominio` (32) → `GlobalExceptionHandler.estadoDe` (83, 86, 89-90) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `unErrorInesperadoResponde500SinMostrarElDetalle` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.inesperado` (74, 78-79) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `validacionDevuelveLosCamposOrdenados` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.validacion` (47-51) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `violacionDeIntegridadResponde409` | Triste | `new GlobalExceptionHandler` (26) → `GlobalExceptionHandler.integridad` (67) → `GlobalExceptionHandler.respuesta` (97) → `ErrorApi.de` (13) → `new ErrorApi` (9) |

### [UsuarioControllerTest](../login/src/test/java/com/equinos/login/infraestructure/entry_points/UsuarioControllerTest.java)

Endpoints del CRUD de usuarios, cambio de contraseña y disponibilidad de correo.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarEnviaLosDatosDeLaPeticion` | Feliz | `UsuarioController.actualizar` (50) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) |
| `cambiarPasswordRespondeSinContenido` | Feliz | `UsuarioController.cambiarPassword` (66-67) |
| `disponibilidadIndicaQueElCorreoEstaLibreYLoNormaliza` | Feliz | `UsuarioController.disponibilidad` (73) → `Usuario.normalizarCorreo` (43) → `DisponibilidadResponse.de` (9-10) → `new DisponibilidadResponse` (4) |
| `disponibilidadIndicaQueElCorreoEstaOcupado` | Feliz | `UsuarioController.disponibilidad` (73) → `Usuario.normalizarCorreo` (43) → `DisponibilidadResponse.de` (9-10) → `new DisponibilidadResponse` (4) |
| `eliminarRespondeSinContenido` | Feliz | `UsuarioController.eliminar` (57-58) |
| `eliminarUnaCuentaInexistenteDejaPasarLaExcepcion` | Triste | Arrange: `new RecursoNoEncontradoException` (7-8) → `UsuarioController.eliminar` (57, sale con excepción) |
| `lasPeticionesEntreganSusDatosAlCasoDeUso` | Feliz | Arrange: `new RegistroUsuarioRequest` (7) → Arrange: `new ActualizarUsuarioRequest` (7) → Arrange: `new CambioPasswordRequest` (6) → `UsuarioController.registrar` (34-36) → `new RegistrarUsuarioCommand` (4) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) → `UsuarioController.actualizar` (50) → `UsuarioController.cambiarPassword` (66-67) |
| `obtenerDevuelveLaCuentaSolicitada` | Feliz | `UsuarioController.obtener` (42) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) |
| `obtenerUnaCuentaAjenaDejaPasarLaExcepcion` | Triste | Arrange: `new RecursoNoEncontradoException` (7-8) → `UsuarioController.obtener` (42, sale con excepción) |
| `registrarRespondeCreadoConLosDatosDelUsuario` | Feliz | `UsuarioController.registrar` (34-36) → `new RegistrarUsuarioCommand` (4) → `UsuarioResponse.de` (11-12) → `new UsuarioResponse` (8) |

## Adaptadores (base de datos, otros servicios y APIs externas) (login)

### [CaballosGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/driver_adapters/external_repository/CaballosGatewayImplTest.java)

Llamada interna a registro-caballo para borrar los caballos de una cuenta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `eliminarLlamaARegistroCaballoConElEncabezadoInterno` | Feliz | `new CaballosGatewayImpl` (26-30) → `CaballosGatewayImpl.eliminarCaballosDeUsuario` (34-35, 37, 42-43) |
| `siRegistroCaballoNoRespondeSeAvisaQueLaCuentaNoSeElimino` | Triste | `new CaballosGatewayImpl` (26-30) → `CaballosGatewayImpl.eliminarCaballosDeUsuario` (34-35, 39-40) → `new ServicioNoDisponibleException` (7-8) |

### [DietasGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/driver_adapters/external_repository/DietasGatewayImplTest.java)

Llamada interna a dieta para borrar las dietas de una cuenta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `eliminarLlamaADietaConElEncabezadoInterno` | Feliz | `new DietasGatewayImpl` (26-30) → `DietasGatewayImpl.eliminarDietasDeUsuario` (34-35, 37, 42-43) |
| `siDietaNoRespondeSeAvisaQueLaCuentaNoSeElimino` | Triste | `new DietasGatewayImpl` (26-30) → `DietasGatewayImpl.eliminarDietasDeUsuario` (34-35, 39-40) → `new ServicioNoDisponibleException` (7-8) |

### [NotificacionesGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/driver_adapters/external_repository/NotificacionesGatewayImplTest.java)

Envío de avisos (bienvenida, contraseña cambiada y cuenta actualizada) al servicio notificaciones.

| Prueba | Camino | Recorrido |
|---|---|---|
| `bienvenidaEnviaElTipoYLaClaveInterna` | Feliz | `new NotificacionesGatewayImpl` (30-35) → `NotificacionesGatewayImpl.bienvenida` (40) → `NotificacionesGatewayImpl.avisar` (63) → `NotificacionesGatewayImpl.enviar` (66-67) → `new NotificacionesGatewayImpl.NotificacionRemota` (82) → `NotificacionesGatewayImpl.publicar` (71-72, 74) |
| `contrasenaCambiadaEnviaSuTipo` | Feliz | `new NotificacionesGatewayImpl` (30-35) → `NotificacionesGatewayImpl.contrasenaCambiada` (45) → `NotificacionesGatewayImpl.avisar` (63) → `NotificacionesGatewayImpl.enviar` (66-67) → `new NotificacionesGatewayImpl.NotificacionRemota` (82) → `NotificacionesGatewayImpl.publicar` (71-72, 74) |
| `cuentaActualizadaConCambioDeCorreoAvisaTambienAlCorreoAnterior` | Feliz | `new NotificacionesGatewayImpl` (30-35) → `NotificacionesGatewayImpl.cuentaActualizada` (50-58) → `NotificacionesGatewayImpl.enviar` (66-67) → `new NotificacionesGatewayImpl.NotificacionRemota` (82) → `NotificacionesGatewayImpl.publicar` (71-72, 74) |
| `cuentaActualizadaSinCambioDeCorreoAvisaUnaVezConLosDatosNuevos` | Feliz | `new NotificacionesGatewayImpl` (30-35) → `NotificacionesGatewayImpl.cuentaActualizada` (50-52, 55-56) → `NotificacionesGatewayImpl.enviar` (66-67) → `new NotificacionesGatewayImpl.NotificacionRemota` (82) → `NotificacionesGatewayImpl.publicar` (71-72, 74) |
| `siNotificacionesNoRespondeNoFallaLaOperacion` | Triste | `new NotificacionesGatewayImpl` (30-35) → `NotificacionesGatewayImpl.bienvenida` (40) → `NotificacionesGatewayImpl.avisar` (63) → `NotificacionesGatewayImpl.enviar` (66-67) → `new NotificacionesGatewayImpl.NotificacionRemota` (82) → `NotificacionesGatewayImpl.publicar` (71-72, 74, 76-77) |

### [PasswordFiltradaGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/driver_adapters/external_repository/PasswordFiltradaGatewayImplTest.java)

API externa Pwned Passwords: revisa si una contraseña apareció en filtraciones.

| Prueba | Camino | Recorrido |
|---|---|---|
| `deshabilitadaNoConsultaLaApi` | Feliz | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43-44) |
| `detectaUnaPasswordFiltrada` | Triste | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) → `PasswordFiltradaGatewayImpl.contiene` (61-62) |
| `lasLineasDeRellenoNoCuentan` | Feliz | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) → `PasswordFiltradaGatewayImpl.contiene` (61-62) |
| `siLaApiFallaLaPasswordSePermite` | Triste | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-51, 53-55) → `PasswordFiltradaGatewayImpl.sha1` (67-68) |
| `soloEnviaElPrefijoYPideRelleno` | Feliz | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) → `PasswordFiltradaGatewayImpl.contiene` (61-62) |
| `unaLineaMalFormadaSeIgnora` | Triste | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) → `PasswordFiltradaGatewayImpl.contiene` (61-62) |
| `unaPasswordQueNoApareceSePermite` | Feliz | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) → `PasswordFiltradaGatewayImpl.contiene` (61-62) |
| `unaRespuestaVaciaPermiteLaPassword` | Triste | `new PasswordFiltradaGatewayImpl` (35-39) → `PasswordFiltradaGatewayImpl.estaFiltrada` (43, 46-48, 50-52) → `PasswordFiltradaGatewayImpl.sha1` (67-68) |

### [UsuarioDataGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/driver_adapters/jpa_repository/UsuarioDataGatewayImplTest.java)

Repositorio de usuarios: traduce entre el dominio y la base de datos.

| Prueba | Camino | Recorrido |
|---|---|---|
| `buscarPorCorreoDevuelveElUsuarioEncontrado` | Feliz | `UsuarioDataGatewayImpl.buscarPorCorreo` (32) |
| `buscarPorCorreoSinResultadoDevuelveVacio` | Triste | `UsuarioDataGatewayImpl.buscarPorCorreo` (32) |
| `buscarPorIdDevuelveElUsuarioEncontrado` | Feliz | `UsuarioDataGatewayImpl.buscarPorId` (27) |
| `buscarPorIdSinResultadoDevuelveVacioYNoConvierte` | Triste | `UsuarioDataGatewayImpl.buscarPorId` (27) |
| `eliminarPorIdBorraEnElRepositorio` | Feliz | `UsuarioDataGatewayImpl.eliminarPorId` (47-48) |
| `existePorCorreoRespondeLoQueDiceElRepositorio` | Feliz | `UsuarioDataGatewayImpl.existePorCorreo` (42) |
| `existePorIdRespondeLoQueDiceElRepositorio` | Feliz | `UsuarioDataGatewayImpl.existePorId` (37) |
| `guardarConvierteGuardaYDevuelveElUsuario` | Feliz | `UsuarioDataGatewayImpl.guardar` (22) |

## Mappers (login)

### [MapperUsuarioTest](../login/src/test/java/com/equinos/login/infraestructure/mapper/MapperUsuarioTest.java)

Copia los datos entre `Usuario` y la entidad `UsuarioData`.

| Prueba | Camino | Recorrido |
|---|---|---|
| `toDataCopiaTodosLosCamposDelUsuario` | Feliz | `new MapperUsuario` (10) → `MapperUsuario.toData` (18-27) |
| `toUsuarioCopiaTodosLosCamposDeLaEntidad` | Feliz | `new MapperUsuario` (10) → `MapperUsuario.toUsuario` (13-14) → `new Usuario` (26-35) |

## Seguridad (login)

### [EncrypterGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/security/EncrypterGatewayImplTest.java)

Encriptación de contraseñas con BCrypt.

| Prueba | Camino | Recorrido |
|---|---|---|
| `coincideReconoceLaContrasenaCorrecta` | Feliz | `EncrypterGatewayImpl.hash` (16) → `EncrypterGatewayImpl.coincide` (21) |
| `hashNoGuardaLaContrasenaEnTextoPlano` | Feliz | `EncrypterGatewayImpl.hash` (16) |

### [JsonAuthenticationEntryPointTest](../login/src/test/java/com/equinos/login/infraestructure/security/JsonAuthenticationEntryPointTest.java)

Respuesta 401 en JSON cuando falta el token o no sirve.

| Prueba | Camino | Recorrido |
|---|---|---|
| `escapaLasComillasDeLaRutaParaNoRomperElJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |
| `respondeNoAutorizadoEnJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |

### [JwtAuthenticationFilterTest](../login/src/test/java/com/equinos/login/infraestructure/security/JwtAuthenticationFilterTest.java)

Filtro que lee el token de cada petición.

| Prueba | Camino | Recorrido |
|---|---|---|
| `sinEncabezadoNoSeLeeNingunToken` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unEncabezadoQueNoEsBearerSeIgnora` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unTokenInvalidoNoAutenticaPeroLaPeticionContinua` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-30, 34-35) |
| `unTokenValidoAutenticaLaPeticion` | Feliz | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-32, 34-35) |

### [JwtServiceTest](../login/src/test/java/com/equinos/login/infraestructure/security/JwtServiceTest.java)

Lectura y validación de tokens JWT. Los tokens son reales y se firman con un secreto aleatorio.

| Prueba | Camino | Recorrido |
|---|---|---|
| `leerDevuelveLosDatosDeUnTokenValido` | Feliz | `JwtService.clave` (29) → `JwtService.leer` (35-40) → `new UsuarioAutenticado` (6) |
| `unTextoQueNoEsTokenSeRechaza` | Triste | `JwtService.leer` (42-43) |
| `unTokenConIdNoNumericoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (35-36, 42-43) |
| `unTokenFirmadoConOtraClaveSeRechaza` | Triste | `new JwtService` (24-26) → `JwtService.clave` (29) → `JwtService.leer` (42-43) |
| `unTokenVencidoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (42-43) |

### [SecurityConfigTest](../login/src/test/java/com/equinos/login/infraestructure/security/SecurityConfigTest.java)

Reglas de seguridad: rutas públicas, sesión sin estado y filtro JWT.

| Prueba | Camino | Recorrido |
|---|---|---|
| `laCadenaAgregaElFiltroJwtAntesDelLoginDeSpring` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `laSesionNoGuardaEstado` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `losErroresDeAutenticacionRespondenEnJson` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) → `new JsonAuthenticationEntryPoint` (12) |
| `noExisteUnUsuarioPorDefecto` | Triste | `SecurityConfig.userDetailsService` (43-44) |
| `soloLasRutasPublicasNoExigenToken` | Feliz | `SecurityConfig.securityFilterChain` (25-37) → `new JwtAuthenticationFilter` (21-23) |

### [TokenGatewayImplTest](../login/src/test/java/com/equinos/login/infraestructure/security/TokenGatewayImplTest.java)

Creación del token JWT al iniciar sesión.

| Prueba | Camino | Recorrido |
|---|---|---|
| `emitirFirmaUnTokenConLosDatosDelUsuarioYSuVigencia` | Feliz | `new TokenGatewayImpl` (23-26) → `TokenGatewayImpl.emitir` (30-39) → `new TokenEmitido` (6) |

## Arranque (login)

### [LoginApplicationTest](../login/src/test/java/com/equinos/login/LoginApplicationTest.java)

Arranque del servicio, con `SpringApplication` simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `mainArrancaLaAplicacion` | Feliz | `LoginApplication.main` (11-12) |

# registro-caballo

## Modelo (registro-caballo)

### [CaballoTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/domain/model/CaballoTest.java)

Reglas del caballo: peso válido, código en mayúsculas y borrado lógico. El caballo es real.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarCambiaTodosLosDatos` | Feliz | `new Caballo` (30-41) → `Caballo.actualizar` (63-69) → `Caballo.normalizarCodigo` (51) → `Caballo.pesoValido` (55, 58) |
| `cambiarEstadoSaludGuardaElEstadoNuevo` | Feliz | `new Caballo` (30-41) → `Caballo.cambiarEstadoSalud` (77-79) |
| `cambiarPesoGuardaElPesoNuevo` | Feliz | `new Caballo` (30-41) → `Caballo.cambiarPeso` (72-74) → `Caballo.pesoValido` (55, 58) |
| `desactivarYReactivarCambianElEstadoActivo` | Feliz | `new Caballo` (30-41) → `Caballo.desactivar` (82-84) → `Caballo.reactivar` (87-89) |
| `noSePuedeRegistrarUnPesoNuloNiCero` | Triste | `Caballo.nuevo` (45, sale con excepción) → `Caballo.normalizarCodigo` (51) → `Caballo.pesoValido` (55-56) → `new ReglaNegocioException` (7-8) |
| `normalizarCodigoConvierteNuloEnVacio` | Triste | `Caballo.normalizarCodigo` (51) |
| `nuevoLimpiaLosDatosYQuedaActivo` | Feliz | `Caballo.nuevo` (45) → `Caballo.normalizarCodigo` (51) → `Caballo.pesoValido` (55, 58) → `new Caballo` (30-41) |
| `unPesoNegativoSeRechazaYConservaElAnterior` | Triste | `new Caballo` (30-41) → `Caballo.cambiarPeso` (72, sale con excepción) → `Caballo.pesoValido` (55-56) → `new ReglaNegocioException` (7-8) |

## Casos de uso (registro-caballo)

### [CaballoUseCaseTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/domain/usecase/CaballoUseCaseTest.java)

CRUD de caballos, reactivación, peso, estado de salud y búsquedas.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarAUnCodigoQueYaUsaOtroCaballoEsConflicto` | Triste | `CaballoUseCase.actualizar` (55-58) → `CaballoUseCase.activoPropio` (138) → `Caballo.normalizarCodigo` (51) → `new ConflictoException` (7-8) |
| `actualizarConservandoElCodigoPropioNoConsultaSiEstaEnUso` | Feliz | `CaballoUseCase.actualizar` (55-57, 60-62) → `CaballoUseCase.activoPropio` (138) → `Caballo.normalizarCodigo` (51) → `Categoria.desde` (18-20) → `EstadoSalud.desde` (18-20) |
| `actualizarEstadoDeSaludAceptaMayusculas` | Feliz | `CaballoUseCase.actualizarEstadoSalud` (101-107, 109) → `CaballoUseCase.activoPropio` (138) → `EstadoSalud.desde` (18-20) |
| `actualizarPesoGuardaElNuevoPeso` | Feliz | `CaballoUseCase.actualizarPeso` (94-96) → `CaballoUseCase.activoPropio` (138) |
| `actualizarPuedeCambiarAUnCodigoLibre` | Feliz | `CaballoUseCase.actualizar` (55-57, 60-62) → `CaballoUseCase.activoPropio` (138) → `Caballo.normalizarCodigo` (51) → `Categoria.desde` (18-20) → `EstadoSalud.desde` (18-20) |
| `buscarConUnFiltroNoValidoSeRechaza` | Triste | `CaballoUseCase.buscar` (115) → `CaballoUseCase.validarPaginacion` (127, 130) → `CaballoUseCase.vacio` (133) → `Categoria.desde` (18-21) → `Categoria.codigo` (14) → `new ReglaNegocioException` (7-8) |
| `buscarConvierteLosFiltrosYUnFiltroVacioNoSeAplica` | Feliz | `CaballoUseCase.buscar` (115-118) → `CaballoUseCase.validarPaginacion` (127, 130) → `CaballoUseCase.vacio` (133) → `Categoria.desde` (18-20) → `EstadoSalud.desde` (18-20) |
| `buscarPorCodigoNormalizaElCodigo` | Feliz | `CaballoUseCase.buscarPorCodigo` (88-89) → `Caballo.normalizarCodigo` (51) |
| `buscarPorUnCodigoQueNoEsDelUsuarioEsNoEncontrado` | Triste | `CaballoUseCase.buscarPorCodigo` (88-89) → `Caballo.normalizarCodigo` (51) → `new RecursoNoEncontradoException` (7-8) |
| `cambiarElEstadoDeSaludAvisaAlUsuario` | Feliz | `CaballoUseCase.actualizarEstadoSalud` (101-107, 109) → `CaballoUseCase.activoPropio` (138) → `EstadoSalud.desde` (18-20) |
| `categoriaOEstadoInvalidosSeRechazanSinGuardar` | Triste | `CaballoUseCase.registrar` (34-35, 38) → `Caballo.normalizarCodigo` (51) → `Categoria.desde` (18-21) → `Categoria.codigo` (14) → `new ReglaNegocioException` (7-8) → `EstadoSalud.desde` (18-21) → `EstadoSalud.codigo` (14) |
| `elCodigoNoSePuedeRepetirAunqueSeaDeOtroUsuario` | Triste | `CaballoUseCase.registrar` (34-36) → `Caballo.normalizarCodigo` (51) → `new ConflictoException` (7-8) |
| `eliminarEsUnaBajaLogica` | Feliz | `CaballoUseCase.eliminar` (67-70) → `CaballoUseCase.activoPropio` (138) |
| `laLimpiezaInternaDevuelveCuantosCaballosBorro` | Feliz | `CaballoUseCase.eliminarTodosDeUsuario` (123) |
| `listarPideLaPaginaAlGateway` | Feliz | `CaballoUseCase.listar` (49-50) → `CaballoUseCase.validarPaginacion` (127, 130) |
| `noSeReactivaSiOtroCaballoActivoTomoElCodigo` | Triste | `CaballoUseCase.reactivar` (74-76, 79-80) → `new ConflictoException` (7-8) |
| `obtenerDevuelveElCaballoActivoDelUsuario` | Feliz | `CaballoUseCase.obtener` (44) → `CaballoUseCase.activoPropio` (138) |
| `reactivarUnCaballoDadoDeBaja` | Feliz | `CaballoUseCase.reactivar` (74-76, 79, 82-83) |
| `reactivarUnCaballoInexistenteOAjenoEsNoEncontrado` | Triste | `CaballoUseCase.reactivar` (74-75) → `new RecursoNoEncontradoException` (7-8) |
| `reactivarUnCaballoQueYaEstaActivoEsConflicto` | Triste | `CaballoUseCase.reactivar` (74-77) → `new ConflictoException` (7-8) |
| `registrarNormalizaElCodigoYDejaElCaballoActivo` | Feliz | `CaballoUseCase.registrar` (34-35, 38-39) → `Caballo.normalizarCodigo` (51) → `Categoria.desde` (18-20) → `EstadoSalud.desde` (18-20) → `Caballo.nuevo` (45) → `Caballo.pesoValido` (55, 58) → `new Caballo` (30-41) |
| `repetirElMismoEstadoDeSaludNoAvisa` | Feliz | `CaballoUseCase.actualizarEstadoSalud` (101-106, 109) → `CaballoUseCase.activoPropio` (138) → `EstadoSalud.desde` (18-20) |
| `unCaballoAjenoBorradoOInexistenteSeVeComoNoEncontrado` | Triste | `CaballoUseCase.obtener` (44, sale con excepción) → `CaballoUseCase.activoPropio` (138) → `new RecursoNoEncontradoException` (7-8) |
| `unCaballoDadoDeBajaNoSePuedeModificarNiEliminar` | Triste | `CaballoUseCase.actualizar` (55, sale con excepción) → `CaballoUseCase.activoPropio` (138) → `new RecursoNoEncontradoException` (7-8) → `CaballoUseCase.actualizarPeso` (94, sale con excepción) → `CaballoUseCase.actualizarEstadoSalud` (101, sale con excepción) → `CaballoUseCase.eliminar` (67, sale con excepción) |
| `unPesoCeroONegativoSeRechaza` | Triste | Arrange: `new ReglaNegocioException` (7-8) → `CaballoUseCase.actualizarPeso` (94) → `CaballoUseCase.activoPropio` (138) |
| `unaPaginacionNoValidaSeRechazaSinConsultar` | Triste | `CaballoUseCase.listar` (49, sale con excepción) → `CaballoUseCase.validarPaginacion` (127-128) → `new ReglaNegocioException` (7-8) → `CaballoUseCase.buscar` (115, sale con excepción) |

## Controladores y respuestas (registro-caballo)

### [CaballoControllerTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/entry_points/CaballoControllerTest.java)

Endpoints de caballos.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarEnviaLosDatosDeLaPeticion` | Feliz | `CaballoController.actualizar` (62) → `CaballoController.datos` (117-118) → `new DatosCaballo` (6) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `actualizarEstadoSaludEnviaElEstadoDeLaPeticion` | Feliz | `CaballoController.actualizarEstadoSalud` (87) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `actualizarPesoEnviaElPesoDeLaPeticion` | Feliz | `CaballoController.actualizarPeso` (94) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `buscarEnviaLosFiltrosYConvierteLaPagina` | Feliz | Arrange: `new Pagina` (7) → `CaballoController.buscar` (112-113) → `Pagina.convertir` (10) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) → `PaginaResponse.de` (10-11) → `new PaginaResponse` (7) |
| `buscarPorCodigoDevuelveElCaballo` | Feliz | `CaballoController.buscarPorCodigo` (101) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `eliminarRespondeSinContenido` | Feliz | `CaballoController.eliminar` (69-70) |
| `lasPeticionesEntreganSusDatosAlCasoDeUso` | Feliz | Arrange: `new CaballoRequest` (14) → Arrange: `new EstadoSaludRequest` (5) → Arrange: `new PesoRequest` (10) → `CaballoController.actualizar` (62) → `CaballoController.datos` (117-118) → `new DatosCaballo` (6) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) → `CaballoController.actualizarEstadoSalud` (87) → `CaballoController.actualizarPeso` (94) |
| `listarConvierteLaPaginaDeCaballos` | Feliz | Arrange: `new Pagina` (7) → `CaballoController.listar` (55) → `Pagina.convertir` (10) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) → `PaginaResponse.de` (10-11) → `new PaginaResponse` (7) |
| `obtenerDevuelveElCaballoDelUsuario` | Feliz | `CaballoController.obtener` (47) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `obtenerUnCaballoInexistenteDejaPasarLaExcepcion` | Triste | Arrange: `new RecursoNoEncontradoException` (7-8) → `CaballoController.obtener` (47, sale con excepción) |
| `reactivarDevuelveElCaballo` | Feliz | `CaballoController.reactivar` (79) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |
| `registrarRespondeCreadoConElPropietarioDelToken` | Feliz | `CaballoController.registrar` (39-41) → `new Propietario` (4) → `CaballoController.datos` (117-118) → `new DatosCaballo` (6) → `CaballoResponse.de` (12-14) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `new CaballoResponse` (8) |

### [GlobalExceptionHandlerTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/entry_points/GlobalExceptionHandlerTest.java)

Convierte cada excepción en una respuesta HTTP con su código.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conflictoResponde409` | Triste | `new GlobalExceptionHandler` (23) → `new ConflictoException` (7-8) → `GlobalExceptionHandler.dominio` (29) → `GlobalExceptionHandler.estadoDe` (70, 73-74) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `cuerpoIlegibleResponde400` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.cuerpoIlegible` (43) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `lasExcepcionesDeSpringConservanSuCodigo` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.inesperado` (61-63) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `parametroDeTipoInvalidoIndicaSuNombre` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.tipoInvalido` (48-49) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `recursoNoEncontradoResponde404` | Triste | `new GlobalExceptionHandler` (23) → `new RecursoNoEncontradoException` (7-8) → `GlobalExceptionHandler.dominio` (29) → `GlobalExceptionHandler.estadoDe` (70-71) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `reglaDeNegocioResponde400` | Triste | `new GlobalExceptionHandler` (23) → `new ReglaNegocioException` (7-8) → `GlobalExceptionHandler.dominio` (29) → `GlobalExceptionHandler.estadoDe` (70, 73, 76) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `unErrorInesperadoResponde500SinMostrarElDetalle` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.inesperado` (61, 65-66) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `validacionDevuelveLosCamposOrdenados` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.validacion` (34-38) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `violacionDeIntegridadResponde409` | Triste | `new GlobalExceptionHandler` (23) → `GlobalExceptionHandler.integridad` (54) → `GlobalExceptionHandler.respuesta` (81) → `ErrorApi.de` (13) → `new ErrorApi` (9) |

### [InternalCaballoControllerTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/entry_points/InternalCaballoControllerTest.java)

Ruta interna que usa login al eliminar una cuenta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `eliminarPorUsuarioIndicaCuantosCaballosBorro` | Feliz | `InternalCaballoController.eliminarPorUsuario` (25) → `new InternalCaballoController.LimpiezaUsuarioResponse` (28) |

## Adaptadores (base de datos, otros servicios y APIs externas) (registro-caballo)

### [CaballoDataGatewayImplTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/driver_adapters/jpa_repository/CaballoDataGatewayImplTest.java)

Repositorio de caballos, con paginación y filtros.

| Prueba | Camino | Recorrido |
|---|---|---|
| `buscarActivoDevuelveElCaballoEncontrado` | Feliz | `CaballoDataGatewayImpl.buscarActivo` (35) |
| `buscarActivoPorCodigoDevuelveElCaballoEncontrado` | Feliz | `CaballoDataGatewayImpl.buscarActivoPorCodigo` (45) |
| `buscarActivoSinResultadoDevuelveVacioYNoConvierte` | Triste | `CaballoDataGatewayImpl.buscarActivo` (35) |
| `buscarActivosConCategoriaYEstadoAplicaLosDosFiltros` | Feliz | `CaballoDataGatewayImpl.buscarActivos` (61, 63-65, 74) → `CaballoDataGatewayImpl.porNombre` (83) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) → `CaballoDataGatewayImpl.aPagina` (87-88) → `new Pagina` (7) |
| `buscarActivosSinFiltrosListaTodos` | Feliz | `CaballoDataGatewayImpl.buscarActivos` (61, 63, 66, 68, 72, 74) → `CaballoDataGatewayImpl.porNombre` (83) → `CaballoDataGatewayImpl.aPagina` (87-88) → `new Pagina` (7) |
| `buscarActivosSoloConCategoriaAplicaEseFiltro` | Feliz | `CaballoDataGatewayImpl.buscarActivos` (61, 63, 66-67, 74) → `CaballoDataGatewayImpl.porNombre` (83) → `Categoria.codigo` (14) → `CaballoDataGatewayImpl.aPagina` (87-88) → `new Pagina` (7) |
| `buscarActivosSoloConEstadoAplicaEseFiltro` | Feliz | `CaballoDataGatewayImpl.buscarActivos` (61, 63, 66, 68-69, 74) → `CaballoDataGatewayImpl.porNombre` (83) → `EstadoSalud.codigo` (14) → `CaballoDataGatewayImpl.aPagina` (87-88) → `new Pagina` (7) |
| `buscarCualquieraIncluyeLosCaballosBorrados` | Feliz | `CaballoDataGatewayImpl.buscarCualquiera` (40) |
| `eliminarTodosDeUsuarioDevuelveCuantosBorro` | Feliz | `CaballoDataGatewayImpl.eliminarTodosDeUsuario` (79) |
| `existeCodigoActivoRespondeLoQueDiceElRepositorio` | Feliz | `CaballoDataGatewayImpl.existeCodigoActivo` (50) |
| `guardarConvierteGuardaYDevuelveElCaballo` | Feliz | `CaballoDataGatewayImpl.guardar` (30) |
| `listarActivosPideLaPaginaOrdenadaPorNombre` | Feliz | `CaballoDataGatewayImpl.listarActivos` (55) → `CaballoDataGatewayImpl.porNombre` (83) → `CaballoDataGatewayImpl.aPagina` (87-88) → `new Pagina` (7) |

### [NotificacionesGatewayImplTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/driver_adapters/external_repository/NotificacionesGatewayImplTest.java)

Aviso de cambio de estado de salud al servicio notificaciones.

| Prueba | Camino | Recorrido |
|---|---|---|
| `avisaAlUsuarioDeLaSesionConElNuevoEstado` | Feliz | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.estadoSaludCambiado` (42-43, 46-51) → `EstadoSalud.codigo` (14) → `new NotificacionesGatewayImpl.NotificacionRemota` (65) → `NotificacionesGatewayImpl.publicar` (54-55, 57, 61-62) |
| `conOtroTipoDeUsuarioNoAvisa` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.estadoSaludCambiado` (42-44) |
| `siNotificacionesNoRespondeNoFallaLaOperacion` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.estadoSaludCambiado` (42-43, 46-51) → `EstadoSalud.codigo` (14) → `new NotificacionesGatewayImpl.NotificacionRemota` (65) → `NotificacionesGatewayImpl.publicar` (54-55, 59-60, 62) |
| `sinSesionNoAvisa` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.estadoSaludCambiado` (42-44) |

## Mappers (registro-caballo)

### [MapperCaballoTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/mapper/MapperCaballoTest.java)

Copia los datos entre `Caballo` y la entidad `CaballoData`.

| Prueba | Camino | Recorrido |
|---|---|---|
| `toCaballoCopiaLosCamposYConvierteCategoriaYEstado` | Feliz | `new MapperCaballo` (12) → `MapperCaballo.toCaballo` (15-17) → `Categoria.desde` (18-20) → `EstadoSalud.desde` (18-20) → `new Caballo` (30-41) |
| `toDataCopiaLosCamposYGuardaLosCodigos` | Feliz | `new MapperCaballo` (12) → `MapperCaballo.toData` (21-32) → `Categoria.codigo` (14) → `EstadoSalud.codigo` (14) |

## Seguridad (registro-caballo)

### [InternalKeyFilterTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/security/InternalKeyFilterTest.java)

Filtro que protege las rutas internas con `X-Internal-Key`.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conElEncabezadoCorrectoLaPeticionContinua` | Feliz | `InternalKeyFilter.doFilterInternal` (40-43, 53-54) |
| `conUnEncabezadoIncorrectoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |
| `lasRutasInternasPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `lasRutasNormalesNoPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `sinEncabezadoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |

### [JsonAuthenticationEntryPointTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/security/JsonAuthenticationEntryPointTest.java)

Respuesta 401 en JSON cuando falta el token o no sirve.

| Prueba | Camino | Recorrido |
|---|---|---|
| `escapaLasComillasDeLaRutaParaNoRomperElJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |
| `respondeNoAutorizadoEnJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |

### [JwtAuthenticationFilterTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/security/JwtAuthenticationFilterTest.java)

Filtro que lee el token de cada petición.

| Prueba | Camino | Recorrido |
|---|---|---|
| `sinEncabezadoNoSeLeeNingunToken` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unEncabezadoQueNoEsBearerSeIgnora` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unTokenInvalidoNoAutenticaPeroLaPeticionContinua` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-30, 34-35) |
| `unTokenValidoAutenticaLaPeticion` | Feliz | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-32, 34-35) |

### [JwtServiceTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/security/JwtServiceTest.java)

Lectura y validación de tokens JWT. Los tokens son reales y se firman con un secreto aleatorio.

| Prueba | Camino | Recorrido |
|---|---|---|
| `leerDevuelveLosDatosDeUnTokenValido` | Feliz | `JwtService.clave` (29) → `JwtService.leer` (35-40) → `new UsuarioAutenticado` (6) |
| `unTextoQueNoEsTokenSeRechaza` | Triste | `JwtService.leer` (42-43) |
| `unTokenConIdNoNumericoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (35-36, 42-43) |
| `unTokenFirmadoConOtraClaveSeRechaza` | Triste | `new JwtService` (24-26) → `JwtService.clave` (29) → `JwtService.leer` (42-43) |
| `unTokenVencidoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (42-43) |

### [SecurityConfigTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/infraestructure/security/SecurityConfigTest.java)

Reglas de seguridad: rutas públicas, sesión sin estado y filtro JWT.

| Prueba | Camino | Recorrido |
|---|---|---|
| `laCadenaAgregaElFiltroJwtAntesDelLoginDeSpring` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `laSesionNoGuardaEstado` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `lasRutasInternasNoUsanToken` | Feliz | `SecurityConfig.securityFilterChain` (25-31, 33-37) → `new JwtAuthenticationFilter` (21-23) |
| `losErroresDeAutenticacionRespondenEnJson` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) → `new JsonAuthenticationEntryPoint` (12) |
| `noExisteUnUsuarioPorDefecto` | Triste | `SecurityConfig.userDetailsService` (43-44) |

## Arranque (registro-caballo)

### [RegistroCaballoApplicationTest](../registro-caballo/src/test/java/com/equinos/registrocaballo/RegistroCaballoApplicationTest.java)

Arranque del servicio, con `SpringApplication` simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `mainArrancaLaAplicacion` | Feliz | `RegistroCaballoApplication.main` (11-12) |

# dieta

## Modelo (dieta)

### [ClimaDiaTest](../dieta/src/test/java/com/equinos/dieta/domain/model/ClimaDiaTest.java)

Índice de estrés térmico y recomendación según el clima.

| Prueba | Camino | Recorrido |
|---|---|---|
| `cadaNivelTieneSuRecomendacion` | Feliz | `new ClimaDia` (11) → `ClimaDia.aviso` (37-39) → `ClimaDia.indice` (33) → `ClimaDia.recomendacion` (43-44, 47-48, 50-51, 53) |
| `elAvisoAclaraQueEsUnaZonaDeReferencia` | Feliz | `new ClimaDia` (11) → `ClimaDia.aviso` (37-39) → `ClimaDia.indice` (33) → `ClimaDia.recomendacion` (43, 47-48) |
| `elIndiceSumaGradosFahrenheitYHumedad` | Feliz | `new ClimaDia` (11) → `ClimaDia.indice` (33) |
| `eligeLaHoraMasCalurosaConSuHumedad` | Feliz | `ClimaDia.deLaHoraMasCalurosa` (22-26, 29) → `new ClimaDia` (11) |
| `ignoraLasHorasSinDatos` | Triste | `ClimaDia.deLaHoraMasCalurosa` (22-26, 29) → `new ClimaDia` (11) |
| `sinDatosNoHayClima` | Triste | `ClimaDia.deLaHoraMasCalurosa` (22-23, 29) |

### [DietaTest](../dieta/src/test/java/com/equinos/dieta/domain/model/DietaTest.java)

Cambio de observaciones de una dieta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conObservacionesSoloCambiaLasObservaciones` | Feliz | `new Dieta` (12) → `Dieta.conObservaciones` (17) |

### [PerfilDietaTest](../dieta/src/test/java/com/equinos/dieta/domain/model/PerfilDietaTest.java)

Datos de un perfil del catálogo NRC.

| Prueba | Camino | Recorrido |
|---|---|---|
| `criterioRacionDescribeLosPorcentajesSinCerosDeMas` | Feliz | `new PerfilDieta` (7) → `PerfilDieta.criterioRacion` (23-25) |
| `esInferidoSoloSiLaFuenteLoIndica` | Feliz | `new PerfilDieta` (7) → `PerfilDieta.esInferido` (15) |
| `itemsDevuelveLaListaDelTipoOVacia` | Feliz | `new PerfilDieta` (7) → `PerfilDieta.items` (19) |

### [RacionTest](../dieta/src/test/java/com/equinos/dieta/domain/model/RacionTest.java)

Cálculo de la ración: materia seca, forraje y concentrado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `calcularRepartelaMateriaSecaEntreForrajeYConcentrado` | Feliz | `Racion.calcular` (23, 26-28) → `new Racion` (18) |
| `unPesoNuloCeroONegativoSeRechaza` | Triste | `Racion.calcular` (23-24) → `new ReglaNegocioException` (7-8) |

## Casos de uso (dieta)

### [CatalogoUseCaseTest](../dieta/src/test/java/com/equinos/dieta/domain/usecase/CatalogoUseCaseTest.java)

Consulta y carga inicial del catálogo NRC.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conElCatalogoVacioLoCargaDesdeElArchivoMaestro` | Feliz | `CatalogoUseCase.cargarSiVacio` (32, 35-37) |
| `filtraPorCategoriaIgnorandoMayusculasYEspacios` | Feliz | `CatalogoUseCase.perfiles` (22-27) → `CatalogoUseCase.normalizar` (41) |
| `filtraPorCategoriaYEstadoALaVez` | Feliz | `CatalogoUseCase.perfiles` (22-27) → `CatalogoUseCase.normalizar` (41) |
| `filtraPorEstadoDeSalud` | Feliz | `CatalogoUseCase.perfiles` (22-27) → `CatalogoUseCase.normalizar` (41) |
| `normalizarQuitaEspaciosPasaAMinusculasYConvierteNuloEnVacio` | Feliz | `CatalogoUseCase.normalizar` (41) |
| `siElCatalogoYaTieneDatosNoLeeElArchivoNiGuarda` | Feliz | `CatalogoUseCase.cargarSiVacio` (32-33) |
| `sinFiltrosDevuelveTodoElCatalogo` | Feliz | `CatalogoUseCase.perfiles` (22-27) → `CatalogoUseCase.normalizar` (41) |

### [DietaUseCaseTest](../dieta/src/test/java/com/equinos/dieta/domain/usecase/DietaUseCaseTest.java)

Generar, consultar, actualizar, eliminar y regenerar dietas.

| Prueba | Camino | Recorrido |
|---|---|---|
| `eliminarBorraLaDietaPropia` | Feliz | `DietaUseCase.eliminar` (68, 71-72) |
| `eliminarUnaDietaAjenaNoBorraNada` | Triste | `DietaUseCase.eliminar` (68-69) → `new RecursoNoEncontradoException` (7-8) |
| `generarAgregaElAvisoDeClimaYNotificaAlUsuario` | Feliz | `DietaUseCase.generar` (40-48) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126-127) → `new Dieta` (12) → `new DietaDetallada` (7) |
| `generarConObservacionesVaciasLasGuardaComoNulas` | Feliz | `DietaUseCase.generar` (40-48) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126-127) → `new Dieta` (12) → `new DietaDetallada` (7) |
| `generarCopiaElCaballoYCalculaConSuPesoReal` | Feliz | `DietaUseCase.generar` (40-48) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126, 129-130, 133) → `new Dieta` (12) → `new DietaDetallada` (7) |
| `historialDevuelveLasDietasDelCaballo` | Feliz | Arrange: `new Pagina` (7) → `DietaUseCase.historial` (86-87) → `DietaUseCase.validarPaginacion` (101, 104) → `Pagina.convertir` (10) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) |
| `laLimpiezaInternaDevuelveCuantasDietasBorro` | Feliz | `DietaUseCase.eliminarTodasDeUsuario` (97) |
| `listarDetallaCadaDietaDeLaPagina` | Feliz | Arrange: `new Pagina` (7) → `DietaUseCase.listar` (56-57) → `DietaUseCase.validarPaginacion` (101, 104) → `Pagina.convertir` (10) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) |
| `observacionesEnBlancoSeBorranYLasMuyLargasSeRechazan` | Triste | `DietaUseCase.actualizarObservaciones` (62-63) → `DietaUseCase.propia` (108) → `DietaUseCase.limpiar` (126-127, 129-131) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) → `new ReglaNegocioException` (7-8) |
| `obtenerDevuelveLaDietaConSuPerfil` | Feliz | `DietaUseCase.obtener` (52) → `DietaUseCase.propia` (108) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) |
| `regenerarCreaUnaDietaNuevaConLasMismasObservaciones` | Feliz | `DietaUseCase.regenerar` (81-82) → `DietaUseCase.propia` (108) → `DietaUseCase.generar` (40-48) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126, 129-130, 133) → `new Dieta` (12) → `new DietaDetallada` (7) |
| `siElClimaNoEstaDisponibleLaDietaSeGeneraSinAviso` | Triste | `DietaUseCase.generar` (40-48) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126-127) → `new Dieta` (12) → `new DietaDetallada` (7) |
| `siElPerfilDeUnaDietaYaNoEstaEnElCatalogoSeAvisaDelError` | Triste | `DietaUseCase.obtener` (52, sale con excepción) → `DietaUseCase.propia` (108) → `DietaUseCase.detallar` (113-114) |
| `siRegistroCaballoNoRespondeNoSeGeneraLaDieta` | Triste | Arrange: `new ServicioNoDisponibleException` (7-8) → `DietaUseCase.generar` (40, sale con excepción) |
| `soloSeActualizanLasObservaciones` | Feliz | `DietaUseCase.actualizarObservaciones` (62-63) → `DietaUseCase.propia` (108) → `DietaUseCase.limpiar` (126, 129-130, 133) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) |
| `unCaballoQueNoExisteONoEsDelUsuarioDa404` | Triste | `DietaUseCase.generar` (40-41) → `new RecursoNoEncontradoException` (7-8) |
| `unCaballoSinDietasNoTieneVigente` | Triste | `DietaUseCase.vigente` (91-92) → `new RecursoNoEncontradoException` (7-8) |
| `unaCombinacionSinPerfilSeRechaza` | Triste | `DietaUseCase.generar` (40-41) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `new ReglaNegocioException` (7-8) |
| `unaDietaAjenaSeVeComoInexistente` | Triste | `DietaUseCase.obtener` (52, sale con excepción) → `DietaUseCase.propia` (108) → `new RecursoNoEncontradoException` (7-8) → `DietaUseCase.regenerar` (81, sale con excepción) → `DietaUseCase.actualizarObservaciones` (62, sale con excepción) |
| `unaPaginacionNoValidaSeRechazaSinConsultar` | Triste | `DietaUseCase.listar` (56, sale con excepción) → `DietaUseCase.validarPaginacion` (101-102) → `new ReglaNegocioException` (7-8) → `DietaUseCase.historial` (86, sale con excepción) |
| `unasObservacionesDeMasDe2000CaracteresSeRechazan` | Triste | `DietaUseCase.generar` (40-45) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `DietaUseCase.limpiar` (126, 129-131) → `new ReglaNegocioException` (7-8) |
| `vigenteDevuelveLaDietaMasReciente` | Feliz | `DietaUseCase.vigente` (91-92) → `DietaUseCase.detallar` (113-115) → `new DietaDetallada` (7, 10-11) |
| `vistaPreviaCalculaSinGuardar` | Feliz | `DietaUseCase.vistaPrevia` (75-77) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `Racion.calcular` (23, 26-28) → `new Racion` (18) → `new VistaPrevia` (6) |
| `vistaPreviaSinPerfilSeRechaza` | Triste | `DietaUseCase.vistaPrevia` (75, sale con excepción) → `DietaUseCase.perfilDe` (119-121) → `CatalogoUseCase.normalizar` (41) → `new ReglaNegocioException` (7-8) |

## Controladores y respuestas (dieta)

### [AvisosTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/entry_points/AvisosTest.java)

Texto de advertencia que acompaña cada dieta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conClimaLoAgregaAlFinal` | Feliz | `Avisos.para` (19, 24) |
| `sinClimaDevuelveSoloElAvisoGeneral` | Triste | `Avisos.para` (19, 24) |

### [DietaControllerTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/entry_points/DietaControllerTest.java)

Endpoints de dietas.

| Prueba | Camino | Recorrido |
|---|---|---|
| `actualizarEnviaLasObservacionesDeLaPeticion` | Feliz | `DietaController.actualizar` (62) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `eliminarRespondeSinContenido` | Feliz | `DietaController.eliminar` (69-70) |
| `generarRespondeCreadoConLaDieta` | Feliz | `DietaController.generar` (39-41) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `historialConvierteLaPaginaDelCaballo` | Feliz | Arrange: `new Pagina` (7) → `DietaController.historial` (102-103) → `Pagina.convertir` (10) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) → `PaginaResponse.de` (10-11) → `new PaginaResponse` (7) |
| `lasPeticionesEntreganSusDatosAlCasoDeUso` | Feliz | Arrange: `new GenerarDietaRequest` (7) → Arrange: `new ObservacionesRequest` (5) → Arrange: `new VistaPreviaRequest` (11) → `DietaController.generar` (39-41) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) → `DietaController.actualizar` (62) → `DietaController.vistaPrevia` (78-79) → `VistaPreviaResponse.de` (11-13) → `new VistaPreviaResponse` (7) |
| `listarConvierteLaPaginaDeDietas` | Feliz | Arrange: `new Pagina` (7) → `DietaController.listar` (55) → `Pagina.convertir` (10) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) → `PaginaResponse.de` (10-11) → `new PaginaResponse` (7) |
| `obtenerDevuelveLaDietaConElAvisoGeneral` | Feliz | `DietaController.obtener` (47) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `obtenerUnaDietaInexistenteDejaPasarLaExcepcion` | Triste | Arrange: `new RecursoNoEncontradoException` (7-8) → `DietaController.obtener` (47, sale con excepción) |
| `perfilesDevuelveElCatalogoFiltrado` | Feliz | `DietaController.perfiles` (110) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) |
| `regenerarRespondeCreadoConLaDietaNueva` | Feliz | `DietaController.regenerar` (86) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `unPerfilInferidoAgregaLaAdvertenciaAlAviso` | Feliz | `DietaController.obtener` (47) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `vigenteDevuelveLaDietaMasReciente` | Feliz | `DietaController.vigente` (93) → `DietaResponse.de` (13-17) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19, 24) → `new DietaResponse` (8) |
| `vistaPreviaCalculaSinGuardar` | Feliz | `DietaController.vistaPrevia` (78-79) → `VistaPreviaResponse.de` (11-13) → `RacionResponse.de` (13-14) → `new RacionResponse` (9) → `PerfilResponse.de` (22-32) → `new PerfilResponse.Requerimiento` (17) → `new PerfilResponse` (10) → `Avisos.para` (19) → `new VistaPreviaResponse` (7) |

### [GlobalExceptionHandlerTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/entry_points/GlobalExceptionHandlerTest.java)

Convierte cada excepción en una respuesta HTTP con su código.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conflictoResponde409` | Triste | `new GlobalExceptionHandler` (24) → `new ConflictoException` (7-8) → `GlobalExceptionHandler.dominio` (30) → `GlobalExceptionHandler.estadoDe` (71, 74-75) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `cuerpoIlegibleResponde400` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.cuerpoIlegible` (44) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `lasExcepcionesDeSpringConservanSuCodigo` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.inesperado` (62-64) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `parametroDeTipoInvalidoIndicaSuNombre` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.tipoInvalido` (49-50) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `recursoNoEncontradoResponde404` | Triste | `new GlobalExceptionHandler` (24) → `new RecursoNoEncontradoException` (7-8) → `GlobalExceptionHandler.dominio` (30) → `GlobalExceptionHandler.estadoDe` (71-72) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `reglaDeNegocioResponde400` | Triste | `new GlobalExceptionHandler` (24) → `new ReglaNegocioException` (7-8) → `GlobalExceptionHandler.dominio` (30) → `GlobalExceptionHandler.estadoDe` (71, 74, 77, 80) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `servicioNoDisponibleResponde503` | Triste | `new GlobalExceptionHandler` (24) → `new ServicioNoDisponibleException` (7-8) → `GlobalExceptionHandler.dominio` (30) → `GlobalExceptionHandler.estadoDe` (71, 74, 77-78) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `unErrorInesperadoResponde500SinMostrarElDetalle` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.inesperado` (62, 66-67) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `validacionDevuelveLosCamposOrdenados` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.validacion` (35-39) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `violacionDeIntegridadResponde409` | Triste | `new GlobalExceptionHandler` (24) → `GlobalExceptionHandler.integridad` (55) → `GlobalExceptionHandler.respuesta` (85) → `ErrorApi.de` (13) → `new ErrorApi` (9) |

### [InternalDietaControllerTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/entry_points/InternalDietaControllerTest.java)

Ruta interna que usa login al eliminar una cuenta.

| Prueba | Camino | Recorrido |
|---|---|---|
| `eliminarPorUsuarioIndicaCuantasDietasBorro` | Feliz | `InternalDietaController.eliminarPorUsuario` (25) → `new InternalDietaController.LimpiezaUsuarioResponse` (28) |

## Adaptadores (base de datos, otros servicios y APIs externas) (dieta)

### [CaballoGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/external_repository/CaballoGatewayImplTest.java)

Consulta del caballo en registro-caballo con el token del usuario.

| Prueba | Camino | Recorrido |
|---|---|---|
| `buscarDevuelveElCaballoYReenviaElTokenDelUsuario` | Feliz | `new CaballoGatewayImpl` (33-36) → `CaballoGatewayImpl.buscar` (41-45, 48-49) → `CaballoGatewayImpl.tokenActual` (59-60) → `new CaballoInfo` (6) |
| `laRespuestaDeRegistroCaballoSeConvierteEnCaballoInfo` | Feliz | `new CaballoGatewayImpl` (33-36) → `new CaballoGatewayImpl.CaballoRemoto` (65) → `CaballoGatewayImpl.buscar` (41-45, 48-49) → `CaballoGatewayImpl.tokenActual` (59-60) → `new CaballoInfo` (6) |
| `siRegistroCaballoNoRespondeSeAvisaQueNoEstaDisponible` | Triste | `new CaballoGatewayImpl` (33-36) → `CaballoGatewayImpl.buscar` (41-42, 52-53) → `CaballoGatewayImpl.tokenActual` (59-60) → `new ServicioNoDisponibleException` (7-8) |
| `sinSesionSeEnviaElEncabezadoSinToken` | Triste | `new CaballoGatewayImpl` (33-36) → `CaballoGatewayImpl.buscar` (41-45, 48-49) → `CaballoGatewayImpl.tokenActual` (59-60) → `new CaballoInfo` (6) |
| `unCaballoNoEncontradoDevuelveVacio` | Triste | `new CaballoGatewayImpl` (33-36) → `CaballoGatewayImpl.buscar` (41-42, 50-51) → `CaballoGatewayImpl.tokenActual` (59-60) |
| `unaRespuestaSinCuerpoDevuelveVacio` | Triste | `new CaballoGatewayImpl` (33-36) → `CaballoGatewayImpl.buscar` (41-46) → `CaballoGatewayImpl.tokenActual` (59-60) |

### [CatalogoDataGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/jpa_repository/CatalogoDataGatewayImplTest.java)

Repositorio del catálogo NRC.

| Prueba | Camino | Recorrido |
|---|---|---|
| `buscarPerfilConOtraCategoriaDevuelveVacio` | Triste | `CatalogoDataGatewayImpl.buscarPerfil` (50-52) → `CatalogoDataGatewayImpl.leerDeBase` (70-76) |
| `buscarPerfilEncuentraLaCombinacionDeCategoriaYEstado` | Feliz | `CatalogoDataGatewayImpl.buscarPerfil` (50-52) → `CatalogoDataGatewayImpl.leerDeBase` (70-76) |
| `buscarPerfilPorIdEncuentraElPerfil` | Feliz | `CatalogoDataGatewayImpl.buscarPerfilPorId` (57) → `CatalogoDataGatewayImpl.leerDeBase` (70-76) |
| `buscarPerfilPorIdInexistenteDevuelveVacio` | Triste | `CatalogoDataGatewayImpl.buscarPerfilPorId` (57) → `CatalogoDataGatewayImpl.leerDeBase` (70-76) |
| `estaVacioSiNoHayPerfiles` | Feliz | `CatalogoDataGatewayImpl.estaVacio` (32) |
| `guardarInsertaCategoriasYEstadosAntesQueLosPerfiles` | Feliz | `CatalogoDataGatewayImpl.guardar` (38-41, 43-46) → `new CategoriaData` (31-36) → `new EstadoSaludData` (25-28) |
| `listarPerfilesDevuelveLosPerfilesConElNombreDeSuEstado` | Feliz | `CatalogoDataGatewayImpl.listarPerfiles` (62) → `CatalogoDataGatewayImpl.leerDeBase` (70-76) |
| `noEstaVacioSiHayPerfiles` | Feliz | `CatalogoDataGatewayImpl.estaVacio` (32) |

### [CatalogoFuenteGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/json_file/CatalogoFuenteGatewayImplTest.java)

Lectura del archivo maestro JSON del catálogo. El archivo es simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `leerConvierteCategoriasEstadosYPerfiles` | Feliz | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64, 66-70, 72-78, 80, 83-85) → `CatalogoFuenteGatewayImpl.objeto` (110, 113) → `CatalogoFuenteGatewayImpl.texto` (117-118) → `new CategoriaCatalogo` (3) → `CatalogoFuenteGatewayImpl.perfil` (89-91, 93-96, 98-105) → `CatalogoFuenteGatewayImpl.decimal` (129-130, 133) → `CatalogoFuenteGatewayImpl.textos` (122-123, 125) → `new RequerimientoNrc` (9) → `Porcentajes.desdeEjemplo` (20-22) → `new Porcentajes` (10) → `new PerfilDieta` (7) → `new EstadoSaludCatalogo` (3) → `new CatalogoMaestro` (6) → `PerfilDieta.esInferido` (15) → `PerfilDieta.items` (19) |
| `siElArchivoNoSePuedeLeerSeIndicaCual` | Triste | `CatalogoFuenteGatewayImpl.leer` (58-59) |
| `unCampoDeTextoAusenteQuedaVacio` | Triste | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64, 66-70, 72-78, 80, 83-85) → `CatalogoFuenteGatewayImpl.objeto` (110, 113) → `CatalogoFuenteGatewayImpl.texto` (117-118) → `new CategoriaCatalogo` (3) → `CatalogoFuenteGatewayImpl.perfil` (89-91, 93-96, 98-105) → `CatalogoFuenteGatewayImpl.decimal` (129-130, 133) → `CatalogoFuenteGatewayImpl.textos` (122-123, 125) → `new RequerimientoNrc` (9) → `Porcentajes.desdeEjemplo` (20-22) → `new Porcentajes` (10) → `new PerfilDieta` (7) → `new EstadoSaludCatalogo` (3) → `new CatalogoMaestro` (6) |
| `unCatalogoSinPerfilesSeRechaza` | Triste | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64, 66-70, 72, 78, 80-81) → `CatalogoFuenteGatewayImpl.objeto` (110, 113) → `CatalogoFuenteGatewayImpl.texto` (117-118) → `new CategoriaCatalogo` (3) |
| `unJsonQueNoEsUnObjetoSeRechaza` | Triste | `CatalogoFuenteGatewayImpl.leer` (56, 58-59) |
| `unNumeroAusenteSeRechazaIndicandoElCampo` | Triste | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64, 66-70, 72-75) → `CatalogoFuenteGatewayImpl.objeto` (110, 113) → `CatalogoFuenteGatewayImpl.texto` (117-118) → `new CategoriaCatalogo` (3) → `CatalogoFuenteGatewayImpl.perfil` (89-90) → `CatalogoFuenteGatewayImpl.decimal` (129-131) |
| `unaEstructuraInesperadaSeRechaza` | Triste | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64) → `CatalogoFuenteGatewayImpl.objeto` (110-111, 113) |
| `unaListaAusenteOConOtroTipoQuedaVacia` | Triste | `CatalogoFuenteGatewayImpl.leer` (56-57, 60, 62-64, 66-70, 72-78, 80, 83-85) → `CatalogoFuenteGatewayImpl.objeto` (110, 113) → `CatalogoFuenteGatewayImpl.texto` (117-118) → `new CategoriaCatalogo` (3) → `CatalogoFuenteGatewayImpl.perfil` (89-91, 93-96, 98-105) → `CatalogoFuenteGatewayImpl.decimal` (129-130, 133) → `CatalogoFuenteGatewayImpl.textos` (122-123, 125) → `new RequerimientoNrc` (9) → `Porcentajes.desdeEjemplo` (20-22) → `new Porcentajes` (10) → `new PerfilDieta` (7) → `new EstadoSaludCatalogo` (3) → `new CatalogoMaestro` (6) → `PerfilDieta.items` (19) |

### [ClimaGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/external_repository/ClimaGatewayImplTest.java)

API externa Open-Meteo, con memoria temporal de 60 minutos.

| Prueba | Camino | Recorrido |
|---|---|---|
| `alVencerLaVigenciaConsultaDeNuevo` | Feliz | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-75, 78) → `ClimaDia.deLaHoraMasCalurosa` (22-26, 29) → `new ClimaDia` (11) |
| `deshabilitadoNoConsultaLaApi` | Feliz | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59-60) |
| `devuelveLaHoraMasCalurosaDelDia` | Feliz | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-75, 78) → `ClimaDia.deLaHoraMasCalurosa` (22-26, 29) → `new ClimaDia` (11) |
| `mientrasEstaVigenteNoVuelveAConsultar` | Feliz | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-64, 66-68) → `ClimaGatewayImpl.consultar` (73-75, 78) → `ClimaDia.deLaHoraMasCalurosa` (22-26, 29) → `new ClimaDia` (11) |
| `siLaApiFallaNoHayClimaYEsperaAntesDeReintentar` | Triste | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-64, 66-68) → `ClimaGatewayImpl.consultar` (79-81) |
| `unaRespuestaSinHorasNoTieneClima` | Triste | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-74, 76) |
| `unaRespuestaSinHumedadesNoTieneClima` | Triste | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-76) |
| `unaRespuestaSinTemperaturasNoTieneClima` | Triste | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-74, 76) |
| `unaRespuestaVaciaNoTieneClima` | Triste | `new ClimaGatewayImpl` (40-41, 46-54) → `ClimaGatewayImpl.climaDelDia` (59, 62-63, 66-68) → `ClimaGatewayImpl.consultar` (73-74, 76) |

### [DietaDataGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/jpa_repository/DietaDataGatewayImplTest.java)

Repositorio de dietas, con paginación.

| Prueba | Camino | Recorrido |
|---|---|---|
| `buscarDevuelveLaDietaEncontrada` | Feliz | `DietaDataGatewayImpl.buscar` (30) → `DietaDataGatewayImpl.aDominio` (75-77) → `new Racion` (18) → `new Dieta` (12) |
| `buscarSinResultadoDevuelveVacio` | Triste | `DietaDataGatewayImpl.buscar` (30) |
| `buscarVigenteDevuelveLaDietaMasReciente` | Feliz | `DietaDataGatewayImpl.buscarVigente` (51-52) → `DietaDataGatewayImpl.aDominio` (75-77) → `new Racion` (18) → `new Dieta` (12) |
| `buscarVigenteSinDietasDevuelveVacio` | Triste | `DietaDataGatewayImpl.buscarVigente` (51-52) |
| `eliminarBorraEnElRepositorio` | Feliz | `DietaDataGatewayImpl.eliminar` (57-58) |
| `eliminarPorUsuarioDevuelveCuantasBorro` | Feliz | `DietaDataGatewayImpl.eliminarPorUsuario` (62) |
| `existeRespondeLoQueDiceElRepositorio` | Feliz | `DietaDataGatewayImpl.existe` (35) |
| `guardarConvierteTodosLosCamposYDevuelveLaDieta` | Feliz | `DietaDataGatewayImpl.guardar` (25) → `DietaDataGatewayImpl.aEntidad` (81-96) → `DietaDataGatewayImpl.aDominio` (75-77) → `new Racion` (18) → `new Dieta` (12) |
| `listarPideLasDietasMasRecientesPrimero` | Feliz | `DietaDataGatewayImpl.listar` (40) → `DietaDataGatewayImpl.recientesPrimero` (66) → `DietaDataGatewayImpl.aPagina` (70-71) → `DietaDataGatewayImpl.aDominio` (75-77) → `new Racion` (18) → `new Dieta` (12) → `new Pagina` (7) |
| `listarPorCaballoPideElHistorialMasRecientePrimero` | Feliz | `DietaDataGatewayImpl.listarPorCaballo` (45-46) → `DietaDataGatewayImpl.recientesPrimero` (66) → `DietaDataGatewayImpl.aPagina` (70-71) → `DietaDataGatewayImpl.aDominio` (75-77) → `new Racion` (18) → `new Dieta` (12) → `new Pagina` (7) |

### [NotificacionesGatewayImplTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/driver_adapters/external_repository/NotificacionesGatewayImplTest.java)

Aviso de dieta generada al servicio notificaciones.

| Prueba | Camino | Recorrido |
|---|---|---|
| `avisaAlUsuarioDeLaSesionConLaRacion` | Feliz | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.dietaGenerada` (42-43, 46-53) → `new NotificacionesGatewayImpl.NotificacionRemota` (67) → `NotificacionesGatewayImpl.publicar` (56-57, 59, 63-64) |
| `conOtroTipoDeUsuarioNoAvisa` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.dietaGenerada` (42-44) |
| `siNotificacionesNoRespondeNoFallaLaOperacion` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.dietaGenerada` (42-43, 46-53) → `new NotificacionesGatewayImpl.NotificacionRemota` (67) → `NotificacionesGatewayImpl.publicar` (56-57, 61-62, 64) |
| `sinSesionNoAvisa` | Triste | `new NotificacionesGatewayImpl` (33-38) → `NotificacionesGatewayImpl.dietaGenerada` (42-44) |

## Mappers (dieta)

### [MapperCatalogoTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/mapper/MapperCatalogoTest.java)

Copia los datos entre `PerfilDieta` y sus entidades.

| Prueba | Camino | Recorrido |
|---|---|---|
| `toDataCopiaLosCamposYNumeraLosItemsDeCadaTipo` | Feliz | `new MapperCatalogo` (19) → `MapperCatalogo.toData` (35-52, 55) → `TipoItem.codigo` (11) → `new PerfilDietaItemData` (40-45) |
| `toPerfilDietaCopiaLosCamposYAgrupaLosItemsPorTipo` | Feliz | `new MapperCatalogo` (19) → `MapperCatalogo.toPerfilDieta` (22-31) → `TipoItem.desde` (15) → `new RequerimientoNrc` (9) → `new Porcentajes` (10) → `new PerfilDieta` (7) → `PerfilDieta.items` (19) |

## Seguridad (dieta)

### [InternalKeyFilterTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/security/InternalKeyFilterTest.java)

Filtro que protege las rutas internas con `X-Internal-Key`.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conElEncabezadoCorrectoLaPeticionContinua` | Feliz | `InternalKeyFilter.doFilterInternal` (40-43, 53-54) |
| `conUnEncabezadoIncorrectoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |
| `lasRutasInternasPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `lasRutasNormalesNoPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `sinEncabezadoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |

### [JsonAuthenticationEntryPointTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/security/JsonAuthenticationEntryPointTest.java)

Respuesta 401 en JSON cuando falta el token o no sirve.

| Prueba | Camino | Recorrido |
|---|---|---|
| `escapaLasComillasDeLaRutaParaNoRomperElJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |
| `respondeNoAutorizadoEnJson` | Triste | `JsonAuthenticationEntryPoint.commence` (17-21, 24) |

### [JwtAuthenticationFilterTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/security/JwtAuthenticationFilterTest.java)

Filtro que lee el token de cada petición.

| Prueba | Camino | Recorrido |
|---|---|---|
| `sinEncabezadoNoSeLeeNingunToken` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unEncabezadoQueNoEsBearerSeIgnora` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-29, 34-35) |
| `unTokenInvalidoNoAutenticaPeroLaPeticionContinua` | Triste | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-30, 34-35) |
| `unTokenValidoAutenticaLaPeticion` | Feliz | `new JwtAuthenticationFilter` (21-23) → `JwtAuthenticationFilter.doFilterInternal` (28-32, 34-35) |

### [JwtServiceTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/security/JwtServiceTest.java)

Lectura y validación de tokens JWT. Los tokens son reales y se firman con un secreto aleatorio.

| Prueba | Camino | Recorrido |
|---|---|---|
| `leerDevuelveLosDatosDeUnTokenValido` | Feliz | `JwtService.clave` (29) → `JwtService.leer` (35-40) → `new UsuarioAutenticado` (6) |
| `unTextoQueNoEsTokenSeRechaza` | Triste | `JwtService.leer` (42-43) |
| `unTokenConIdNoNumericoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (35-36, 42-43) |
| `unTokenFirmadoConOtraClaveSeRechaza` | Triste | `new JwtService` (24-26) → `JwtService.clave` (29) → `JwtService.leer` (42-43) |
| `unTokenVencidoSeRechaza` | Triste | `JwtService.clave` (29) → `JwtService.leer` (42-43) |

### [SecurityConfigTest](../dieta/src/test/java/com/equinos/dieta/infraestructure/security/SecurityConfigTest.java)

Reglas de seguridad: rutas públicas, sesión sin estado y filtro JWT.

| Prueba | Camino | Recorrido |
|---|---|---|
| `laCadenaAgregaElFiltroJwtAntesDelLoginDeSpring` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `laSesionNoGuardaEstado` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) |
| `lasRutasInternasNoUsanToken` | Feliz | `SecurityConfig.securityFilterChain` (25-31, 33-37) → `new JwtAuthenticationFilter` (21-23) |
| `losErroresDeAutenticacionRespondenEnJson` | Feliz | `SecurityConfig.securityFilterChain` (25-30, 35-37) → `new JwtAuthenticationFilter` (21-23) → `new JsonAuthenticationEntryPoint` (12) |
| `noExisteUnUsuarioPorDefecto` | Triste | `SecurityConfig.userDetailsService` (43-44) |

## Arranque (dieta)

### [DietaApplicationTest](../dieta/src/test/java/com/equinos/dieta/DietaApplicationTest.java)

Arranque del servicio, con `SpringApplication` simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `mainArrancaLaAplicacion` | Feliz | `DietaApplication.main` (11-12) |

# notificaciones

## Modelo (notificaciones)

### [NotificacionTest](../notificaciones/src/test/java/com/equinos/notificaciones/domain/model/NotificacionTest.java)

Datos de una notificación.

| Prueba | Camino | Recorrido |
|---|---|---|
| `sinDatosQuedaUnMapaVacio` | Triste | `new Notificacion` (8-10) |
| `unDatoAusenteSeMuestraConUnGuion` | Triste | Arrange: `new TipoNotificacion` (13-15) → `new Notificacion` (8-10) → `Notificacion.dato` (13) |

## Casos de uso (notificaciones)

### [NotificacionUseCaseTest](../notificaciones/src/test/java/com/equinos/notificaciones/domain/usecase/NotificacionUseCaseTest.java)

Arma el asunto y el texto de cada tipo de correo.

| Prueba | Camino | Recorrido |
|---|---|---|
| `bienvenidaSaludaAlUsuario` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23-24) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |
| `contrasenaCambiadaAlertaAlUsuario` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23, 26) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |
| `dietaGeneradaIncluyeLaRacion` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23, 33-36) → `NotificacionUseCase.caballo` (44) → `Notificacion.dato` (13) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |
| `estadoDeSaludIndicaElNuevoEstado` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23, 38) → `NotificacionUseCase.caballo` (44) → `Notificacion.dato` (13) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |
| `cuentaActualizadaConCambioDeCorreoMuestraElAnterior` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23, 28-32) → `Notificacion.dato` (13) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |
| `cuentaActualizadaMuestraLosDatosNuevos` | Feliz | Arrange: `new Notificacion` (8-10) → `NotificacionUseCase.enviar` (18-20) → `NotificacionUseCase.contenido` (23, 28-32) → `Notificacion.dato` (13) → `TipoNotificacion.asunto` (18) → `new MensajeCorreo` (4) |

## Controladores y respuestas (notificaciones)

### [GlobalExceptionHandlerTest](../notificaciones/src/test/java/com/equinos/notificaciones/infraestructure/entry_points/GlobalExceptionHandlerTest.java)

Convierte cada excepción en una respuesta HTTP con su código.

| Prueba | Camino | Recorrido |
|---|---|---|
| `cuerpoIlegibleResponde400` | Triste | `new GlobalExceptionHandler` (17) → `GlobalExceptionHandler.cuerpoIlegible` (32) → `GlobalExceptionHandler.respuesta` (47) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `lasExcepcionesDeSpringConservanSuCodigo` | Triste | `new GlobalExceptionHandler` (17) → `GlobalExceptionHandler.inesperado` (37-39) → `GlobalExceptionHandler.respuesta` (47) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `unErrorInesperadoResponde500SinMostrarElDetalle` | Triste | `new GlobalExceptionHandler` (17) → `GlobalExceptionHandler.inesperado` (37, 41-42) → `GlobalExceptionHandler.respuesta` (47) → `ErrorApi.de` (13) → `new ErrorApi` (9) |
| `validacionDevuelveLosCamposOrdenados` | Triste | `new GlobalExceptionHandler` (17) → `GlobalExceptionHandler.validacion` (23-27) → `GlobalExceptionHandler.respuesta` (47) → `ErrorApi.de` (13) → `new ErrorApi` (9) |

### [InternalNotificacionControllerTest](../notificaciones/src/test/java/com/equinos/notificaciones/infraestructure/entry_points/InternalNotificacionControllerTest.java)

Ruta interna que reciben los demás servicios para pedir un correo.

| Prueba | Camino | Recorrido |
|---|---|---|
| `elRequestSeConvierteAlDominio` | Feliz | Arrange: `new NotificacionRequest` (13) → Arrange: `NotificacionRequest.aDominio` (24) → Arrange: `new Notificacion` (8-10) → Arrange: `Notificacion.dato` (13) |
| `noEsperaAQueElCorreoSeEnvie` | Feliz | `InternalNotificacionController.recibir` (27-28) |
| `recibeLaNotificacionYRespondeAceptada` | Feliz | `InternalNotificacionController.recibir` (27-28) |

## Adaptadores (base de datos, otros servicios y APIs externas) (notificaciones)

### [CorreoGatewayImplTest](../notificaciones/src/test/java/com/equinos/notificaciones/infraestructure/driver_adapters/correo/CorreoGatewayImplTest.java)

Envío del correo con Spring Mail: versión HTML con el banner y el logo, y versión de texto plano. El servidor de
correo es simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `deshabilitadoNoSeConectaAGmail` | Feliz | `new CorreoGatewayImpl` (34-38) → `CorreoGatewayImpl.enviar` (43-45) |
| `enviaElCorreoHtmlConSusDatosYElLogo` | Feliz | `new CorreoGatewayImpl` (34-38) → `CorreoGatewayImpl.enviar` (43, 47-55) → `PlantillaCorreoHtml.generar` (26-56) → `PlantillaCorreoHtml.cuerpoHtml` (60-81) → `PlantillaCorreoHtml.escapar` (86) |
| `unErrorAlArmarElCorreoNoSePropagaNiSeEnvia` | Triste | `new CorreoGatewayImpl` (34-38) → `CorreoGatewayImpl.enviar` (43, 47-50, 56-57) |
| `unFalloDeEnvioNoSePropaga` | Triste | `new CorreoGatewayImpl` (34-38) → `CorreoGatewayImpl.enviar` (43, 47-57) → `PlantillaCorreoHtml.generar` (26-56) |

### [PlantillaCorreoHtmlTest](../notificaciones/src/test/java/com/equinos/notificaciones/infraestructure/driver_adapters/correo/PlantillaCorreoHtmlTest.java)

Plantilla HTML del correo: banner con el logo, párrafos, listas y escape de caracteres especiales.

| Prueba | Camino | Recorrido |
|---|---|---|
| `escapaLosCaracteresEspecialesDeHtml` | Feliz | `PlantillaCorreoHtml.generar` (26-56) → `PlantillaCorreoHtml.escapar` (86) → `PlantillaCorreoHtml.cuerpoHtml` (60-81) |
| `generaElBannerConElLogoYElTitulo` | Feliz | `PlantillaCorreoHtml.generar` (26-56) → `PlantillaCorreoHtml.escapar` (86) → `PlantillaCorreoHtml.cuerpoHtml` (60-81) |
| `separaParrafosYConvierteLasLineasConGuionEnLista` | Feliz | `PlantillaCorreoHtml.cuerpoHtml` (60-81) → `PlantillaCorreoHtml.escapar` (86) |
| `unParrafoDeVariasLineasLasUneConSaltoDeLinea` | Feliz | `PlantillaCorreoHtml.cuerpoHtml` (60-81) → `PlantillaCorreoHtml.escapar` (86) |
| `unaListaSinTextoNoGeneraParrafoVacio` | Feliz | `PlantillaCorreoHtml.cuerpoHtml` (60-81) → `PlantillaCorreoHtml.escapar` (86) |

## Seguridad (notificaciones)

### [InternalKeyFilterTest](../notificaciones/src/test/java/com/equinos/notificaciones/infraestructure/security/InternalKeyFilterTest.java)

Filtro que protege las rutas internas con `X-Internal-Key`.

| Prueba | Camino | Recorrido |
|---|---|---|
| `conElEncabezadoCorrectoLaPeticionContinua` | Feliz | `InternalKeyFilter.doFilterInternal` (40-43, 53-54) |
| `conUnEncabezadoIncorrectoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |
| `lasRutasInternasPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `lasRutasNormalesNoPasanPorElFiltro` | Feliz | `InternalKeyFilter.shouldNotFilter` (34) |
| `sinEncabezadoRespondeNoAutorizado` | Triste | `InternalKeyFilter.doFilterInternal` (40-48, 51) |

## Arranque (notificaciones)

### [NotificacionesApplicationTest](../notificaciones/src/test/java/com/equinos/notificaciones/NotificacionesApplicationTest.java)

Arranque del servicio, con `SpringApplication` simulado.

| Prueba | Camino | Recorrido |
|---|---|---|
| `mainArrancaLaAplicacion` | Feliz | `NotificacionesApplication.main` (10-11) |
