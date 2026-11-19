package com.equinos.login.infraestructure.driver_adapters.jpa_repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.equinos.login.domain.model.Usuario;
import com.equinos.login.infraestructure.mapper.MapperUsuario;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas de UsuarioDataGatewayImpl. El repositorio JPA, el mapper y los datos son simulados. */
@ExtendWith(MockitoExtension.class)
class UsuarioDataGatewayImplTest {

    private static final Long ID = 1L;
    private static final String CORREO = "ana@correo.com";

    @Mock
    private MapperUsuario mapperUsuario;
    @Mock
    private UsuarioDataJpaRepository repositorio;

    @InjectMocks
    private UsuarioDataGatewayImpl gateway;

    @Test
    void guardarConvierteGuardaYDevuelveElUsuario() {
        //Arrange
        Usuario usuario = mock(Usuario.class);
        Usuario usuarioGuardado = mock(Usuario.class);
        UsuarioData entidad = mock(UsuarioData.class);
        UsuarioData entidadGuardada = mock(UsuarioData.class);
        when(mapperUsuario.toData(usuario)).thenReturn(entidad);
        when(repositorio.save(entidad)).thenReturn(entidadGuardada);
        when(mapperUsuario.toUsuario(entidadGuardada)).thenReturn(usuarioGuardado);

        //Act
        Usuario resultado = gateway.guardar(usuario);

        //Assert
        assertSame(usuarioGuardado, resultado);
        verify(repositorio).save(entidad);
    }

    @Test
    void buscarPorIdDevuelveElUsuarioEncontrado() {
        //Arrange
        Usuario usuario = mock(Usuario.class);
        UsuarioData entidad = mock(UsuarioData.class);
        when(repositorio.findById(ID)).thenReturn(Optional.of(entidad));
        when(mapperUsuario.toUsuario(entidad)).thenReturn(usuario);

        //Act
        Optional<Usuario> resultado = gateway.buscarPorId(ID);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(usuario, resultado.get());
    }

    @Test
    void buscarPorIdSinResultadoDevuelveVacioYNoConvierte() {
        //Arrange
        when(repositorio.findById(ID)).thenReturn(Optional.empty());

        //Act
        Optional<Usuario> resultado = gateway.buscarPorId(ID);

        //Assert
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(mapperUsuario);
    }

    @Test
    void buscarPorCorreoDevuelveElUsuarioEncontrado() {
        //Arrange
        Usuario usuario = mock(Usuario.class);
        UsuarioData entidad = mock(UsuarioData.class);
        when(repositorio.findByCorreo(CORREO)).thenReturn(Optional.of(entidad));
        when(mapperUsuario.toUsuario(any(UsuarioData.class))).thenReturn(usuario);

        //Act
        Optional<Usuario> resultado = gateway.buscarPorCorreo(CORREO);

        //Assert
        assertTrue(resultado.isPresent());
        assertSame(usuario, resultado.get());
    }

    @Test
    void buscarPorCorreoSinResultadoDevuelveVacio() {
        //Arrange
        when(repositorio.findByCorreo(CORREO)).thenReturn(Optional.empty());

        //Act
        Optional<Usuario> resultado = gateway.buscarPorCorreo(CORREO);

        //Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void existePorIdRespondeLoQueDiceElRepositorio() {
        //Arrange
        when(repositorio.existsById(ID)).thenReturn(true);
        when(repositorio.existsById(2L)).thenReturn(false);

        //Act y Assert
        assertTrue(gateway.existePorId(ID));
        assertFalse(gateway.existePorId(2L));
    }

    @Test
    void existePorCorreoRespondeLoQueDiceElRepositorio() {
        //Arrange
        when(repositorio.existsByCorreo(CORREO)).thenReturn(true);
        when(repositorio.existsByCorreo("otro@correo.com")).thenReturn(false);

        //Act y Assert
        assertTrue(gateway.existePorCorreo(CORREO));
        assertFalse(gateway.existePorCorreo("otro@correo.com"));
    }

    @Test
    void eliminarPorIdBorraEnElRepositorio() {
        //Act
        gateway.eliminarPorId(ID);

        //Assert
        verify(repositorio).deleteById(ID);
    }
}
