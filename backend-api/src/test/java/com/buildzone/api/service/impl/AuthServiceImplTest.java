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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.buildzone.api.Fixtures;
import com.buildzone.api.dto.AuthResponse;
import com.buildzone.api.dto.LoginRequest;
import com.buildzone.api.dto.RegistroRequest;
import com.buildzone.api.exception.AccesoDenegadoException;
import com.buildzone.api.exception.CredencialesInvalidasException;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.model.enums.EstadoUsuario;
import com.buildzone.api.model.enums.Rol;
import com.buildzone.api.repository.UsuarioRepository;
import com.buildzone.api.security.JwtService;

/** Pruebas unitarias del modulo de autenticacion. */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Registrar: crea la cuenta como USUARIO ACTIVO, con contrasena cifrada y correo en minuscula")
    void registrarCreaUsuario() {
        RegistroRequest request = new RegistroRequest("Ana", "Gomez", "ana.gomez", "Ana@Mail.com", "Secreta123");
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(false);
        when(usuarioRepository.existsByUsernameIgnoreCase("ana.gomez")).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("HASH-BCRYPT");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-jwt");

        AuthResponse respuesta = authService.registrar(request);

        assertThat(respuesta.token()).isEqualTo("token-jwt");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.usuario().id()).isEqualTo(10L);
        assertThat(respuesta.usuario().email()).isEqualTo("ana@mail.com");
        assertThat(respuesta.usuario().rol()).isEqualTo(Rol.USUARIO);
        assertThat(respuesta.usuario().estado()).isEqualTo(EstadoUsuario.ACTIVO);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASH-BCRYPT");
    }

    @Test
    @DisplayName("Registrar: rechaza un correo ya registrado")
    void registrarCorreoDuplicado() {
        RegistroRequest request = new RegistroRequest("Ana", "Gomez", "ana", "ana@mail.com", "Secreta123");
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("correo");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar: rechaza un nombre de usuario ya usado")
    void registrarUsernameDuplicado() {
        RegistroRequest request = new RegistroRequest("Ana", "Gomez", "ana", "ana@mail.com", "Secreta123");
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(false);
        when(usuarioRepository.existsByUsernameIgnoreCase("ana")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("Login: acepta el correo como identificador")
    void loginConCorreo() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findByEmailIgnoreCase("ana@buildzone.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("Secreta123", "HASH-1")).thenReturn(true);
        when(jwtService.generarToken(ana)).thenReturn("token-ana");

        AuthResponse respuesta = authService.iniciarSesion(new LoginRequest("ana@buildzone.com", "Secreta123"));

        assertThat(respuesta.token()).isEqualTo("token-ana");
        assertThat(respuesta.usuario().username()).isEqualTo("ana");
    }

    @Test
    @DisplayName("Login: acepta el nombre de usuario como identificador")
    void loginConUsername() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findByUsernameIgnoreCase("ana")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("Secreta123", "HASH-1")).thenReturn(true);
        when(jwtService.generarToken(ana)).thenReturn("token-ana");

        assertThat(authService.iniciarSesion(new LoginRequest("ana", "Secreta123")).token())
                .isEqualTo("token-ana");
    }

    @Test
    @DisplayName("Login: contrasena incorrecta lanza CredencialesInvalidas")
    void loginPasswordIncorrecta() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        when(usuarioRepository.findByUsernameIgnoreCase("ana")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("mala", "HASH-1")).thenReturn(false);

        assertThatThrownBy(() -> authService.iniciarSesion(new LoginRequest("ana", "mala")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Login: usuario inexistente lanza CredencialesInvalidas (mismo mensaje, no revela si existe)")
    void loginUsuarioInexistente() {
        when(usuarioRepository.findByUsernameIgnoreCase("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.iniciarSesion(new LoginRequest("nadie", "x")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Login: una cuenta INACTIVO no puede iniciar sesion")
    void loginCuentaInactiva() {
        Usuario ana = Fixtures.usuario(1L, "ana", Rol.USUARIO);
        ana.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepository.findByUsernameIgnoreCase("ana")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("Secreta123", "HASH-1")).thenReturn(true);

        assertThatThrownBy(() -> authService.iniciarSesion(new LoginRequest("ana", "Secreta123")))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(jwtService, never()).generarToken(any());
    }
}
