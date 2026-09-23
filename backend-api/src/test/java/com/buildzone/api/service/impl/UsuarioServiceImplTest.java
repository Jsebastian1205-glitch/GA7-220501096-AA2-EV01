package com.buildzone.api.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.buildzone.api.Fixtures;
import com.buildzone.api.dto.CambioPasswordRequest;
import com.buildzone.api.dto.PerfilRequest;
import com.buildzone.api.dto.UsuarioResponse;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.exception.RecursoNoEncontradoException;
import com.buildzone.api.exception.ReglaNegocioException;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.EstadoUsuario;
import com.buildzone.api.model.enums.Rol;
import com.buildzone.api.repository.UsuarioRepository;

/** Pruebas unitarias del modulo de usuarios (perfil y administracion). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("Actualizar perfil: guarda los cambios y normaliza el correo")
    void actualizarPerfil() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@mail.com")).thenReturn(false);
        when(usuarioRepository.save(ana)).thenReturn(ana);

        UsuarioResponse r = usuarioService.actualizarPerfil(1L, new PerfilRequest("Ana Maria", "Gomez", "Nuevo@Mail.com"));

        assertThat(r.nombre()).isEqualTo("Ana Maria");
        assertThat(r.email()).isEqualTo("nuevo@mail.com");
    }

    @Test
    @DisplayName("Actualizar perfil: no permite usar el correo de otra cuenta")
    void actualizarPerfilCorreoDeOtro() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(usuarioRepository.existsByEmailIgnoreCase("luis@buildzone.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizarPerfil(1L,
                new PerfilRequest("Ana", "Gomez", "luis@buildzone.com")))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("Cambiar contrasena: guarda el nuevo hash si la actual es correcta")
    void cambiarPasswordCorrecta() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("Actual123", "HASH-1")).thenReturn(true);
        when(passwordEncoder.encode("Nueva1234")).thenReturn("HASH-NUEVO");

        usuarioService.cambiarPassword(1L, new CambioPasswordRequest("Actual123", "Nueva1234"));

        assertThat(ana.getPasswordHash()).isEqualTo("HASH-NUEVO");
        verify(usuarioRepository).save(ana);
    }

    @Test
    @DisplayName("Cambiar contrasena: rechaza si la contrasena actual no coincide")
    void cambiarPasswordActualIncorrecta() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("mala", "HASH-1")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, new CambioPasswordRequest("mala", "Nueva1234")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("actual");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar contrasena: la nueva debe ser distinta de la actual")
    void cambiarPasswordIgual() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("Igual1234", "HASH-1")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, new CambioPasswordRequest("Igual1234", "Igual1234")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Cambiar rol: el ADMIN puede promover a otro usuario")
    void cambiarRolDeOtro() {
        Usuario luis = Fixtures.usuario(2L, "luis", Rol.USUARIO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(luis));
        when(usuarioRepository.save(luis)).thenReturn(luis);

        assertThat(usuarioService.cambiarRol(1L, 2L, Rol.ADMIN).rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    @DisplayName("Cambiar rol: un ADMIN no puede quitarse su propio rol")
    void noPuedeQuitarseSuPropioRol() {
        assertThatThrownBy(() -> usuarioService.cambiarRol(1L, 1L, Rol.USUARIO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Cambiar estado: un ADMIN no puede desactivarse a si mismo")
    void noPuedeDesactivarse() {
        assertThatThrownBy(() -> usuarioService.cambiarEstado(1L, 1L, EstadoUsuario.INACTIVO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Cambiar estado: usuario inexistente lanza 404")
    void cambiarEstadoUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarEstado(1L, 99L, EstadoUsuario.INACTIVO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
