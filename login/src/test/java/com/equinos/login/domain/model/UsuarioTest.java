package com.equinos.login.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de Usuario. Aquí el usuario es real porque es la clase que se prueba. */
class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant DESPUES = AHORA.plusSeconds(60);

    private static Usuario usuarioExistente() {
        return new Usuario(1L, "Ana", "ana@correo.com", "hash", 0, null, AHORA, AHORA);
    }

    @Test
    void nuevoLimpiaLosDatosYEmpiezaSinIntentosNiBloqueo() {
        //Act
        Usuario usuario = Usuario.nuevo(" Ana ", " ANA@Correo.com ", "hash", AHORA);

        //Assert
        assertNull(usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@correo.com", usuario.getCorreo());
        assertEquals("hash", usuario.getPasswordHash());
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(AHORA, usuario.getCreadoEn());
        assertEquals(AHORA, usuario.getActualizadoEn());
    }

    @Test
    void normalizarCorreoConvierteNuloEnVacio() {
        //Act y Assert
        assertEquals("", Usuario.normalizarCorreo(null));
    }

    @Test
    void sinFechaDeBloqueoNoEstaBloqueado() {
        //Act y Assert
        assertFalse(usuarioExistente().estaBloqueado(AHORA));
    }

    @Test
    void estaBloqueadoSoloMientrasLaFechaNoHayaPasado() {
        //Arrange
        Usuario usuario = new Usuario(1L, "Ana", "ana@correo.com", "hash", 0, DESPUES, AHORA, AHORA);

        //Act y Assert
        assertTrue(usuario.estaBloqueado(AHORA));
        assertFalse(usuario.estaBloqueado(DESPUES));
    }

    @Test
    void losPrimerosIntentosFallidosSoloSeCuentan() {
        //Arrange
        Usuario usuario = usuarioExistente();

        //Act
        boolean bloqueado = usuario.registrarIntentoFallido(DESPUES);

        //Assert
        assertFalse(bloqueado);
        assertEquals(1, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(DESPUES, usuario.getActualizadoEn());
    }

    @Test
    void elTercerIntentoFallidoBloqueaQuinceMinutos() {
        //Arrange
        Usuario usuario = usuarioExistente();
        usuario.registrarIntentoFallido(AHORA);
        usuario.registrarIntentoFallido(AHORA);

        //Act
        boolean bloqueado = usuario.registrarIntentoFallido(AHORA);

        //Assert
        assertTrue(bloqueado);
        assertEquals(0, usuario.getIntentosFallidos());
        assertEquals(AHORA.plus(Duration.ofMinutes(15)), usuario.getBloqueadoHasta());
    }

    @Test
    void unIngresoExitosoReiniciaIntentosYBloqueo() {
        //Arrange
        Usuario usuario = new Usuario(1L, "Ana", "ana@correo.com", "hash", 2, DESPUES, AHORA, AHORA);

        //Act
        usuario.registrarIngresoExitoso(DESPUES);

        //Assert
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(DESPUES, usuario.getActualizadoEn());
    }

    @Test
    void actualizarDatosLimpiaNombreYCorreo() {
        //Arrange
        Usuario usuario = usuarioExistente();

        //Act
        usuario.actualizarDatos(" Ana Maria ", " ANAMARIA@correo.com ", DESPUES);

        //Assert
        assertEquals("Ana Maria", usuario.getNombre());
        assertEquals("anamaria@correo.com", usuario.getCorreo());
        assertEquals(DESPUES, usuario.getActualizadoEn());
    }

    @Test
    void cambiarPasswordGuardaElHashNuevo() {
        //Arrange
        Usuario usuario = usuarioExistente();

        //Act
        usuario.cambiarPassword("hash-nuevo", DESPUES);

        //Assert
        assertEquals("hash-nuevo", usuario.getPasswordHash());
        assertEquals(DESPUES, usuario.getActualizadoEn());
    }
}
