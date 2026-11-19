package com.equinos.login.infraestructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.infraestructure.driver_adapters.jpa_repository.UsuarioData;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de MapperUsuario: el origen de cada conversión es simulado. */
@ExtendWith(MockitoExtension.class)
class MapperUsuarioTest {

    private static final Long ID = 1L;
    private static final Instant BLOQUEO = Instant.parse("2026-01-01T10:15:00Z");
    private static final Instant CREADO = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant ACTUALIZADO = Instant.parse("2026-01-02T10:00:00Z");

    @InjectMocks
    private MapperUsuario mapper;

    @Test
    void toUsuarioCopiaTodosLosCamposDeLaEntidad() {
        //Arrange
        UsuarioData entidad = mock(UsuarioData.class);
        lenient().when(entidad.getId()).thenReturn(ID);
        lenient().when(entidad.getNombre()).thenReturn("Ana");
        lenient().when(entidad.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(entidad.getPasswordHash()).thenReturn("hash");
        lenient().when(entidad.getIntentosFallidos()).thenReturn(2);
        lenient().when(entidad.getBloqueadoHasta()).thenReturn(BLOQUEO);
        lenient().when(entidad.getCreadoEn()).thenReturn(CREADO);
        lenient().when(entidad.getActualizadoEn()).thenReturn(ACTUALIZADO);

        //Act
        Usuario usuario = mapper.toUsuario(entidad);

        //Assert
        assertEquals(ID, usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@correo.com", usuario.getCorreo());
        assertEquals("hash", usuario.getPasswordHash());
        assertEquals(2, usuario.getIntentosFallidos());
        assertEquals(BLOQUEO, usuario.getBloqueadoHasta());
        assertEquals(CREADO, usuario.getCreadoEn());
        assertEquals(ACTUALIZADO, usuario.getActualizadoEn());
    }

    @Test
    void toDataCopiaTodosLosCamposDelUsuario() {
        //Arrange
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(ID);
        lenient().when(usuario.getNombre()).thenReturn("Ana");
        lenient().when(usuario.getCorreo()).thenReturn("ana@correo.com");
        lenient().when(usuario.getPasswordHash()).thenReturn("hash");
        lenient().when(usuario.getIntentosFallidos()).thenReturn(2);
        lenient().when(usuario.getBloqueadoHasta()).thenReturn(BLOQUEO);
        lenient().when(usuario.getCreadoEn()).thenReturn(CREADO);
        lenient().when(usuario.getActualizadoEn()).thenReturn(ACTUALIZADO);

        //Act
        UsuarioData entidad = mapper.toData(usuario);

        //Assert
        assertEquals(ID, entidad.getId());
        assertEquals("Ana", entidad.getNombre());
        assertEquals("ana@correo.com", entidad.getCorreo());
        assertEquals("hash", entidad.getPasswordHash());
        assertEquals(2, entidad.getIntentosFallidos());
        assertEquals(BLOQUEO, entidad.getBloqueadoHasta());
        assertEquals(CREADO, entidad.getCreadoEn());
        assertEquals(ACTUALIZADO, entidad.getActualizadoEn());
    }
}
