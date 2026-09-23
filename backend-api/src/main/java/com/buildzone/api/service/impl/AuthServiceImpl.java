package com.buildzone.api.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildzone.api.dto.AuthResponse;
import com.buildzone.api.dto.LoginRequest;
import com.buildzone.api.dto.RegistroRequest;
import com.buildzone.api.dto.UsuarioResponse;
import com.buildzone.api.exception.AccesoDenegadoException;
import com.buildzone.api.exception.CredencialesInvalidasException;
import com.buildzone.api.exception.RecursoDuplicadoException;
import com.buildzone.api.model.Usuario;
import com.buildzone.api.repository.UsuarioRepository;
import com.buildzone.api.security.JwtService;
import com.buildzone.api.service.AuthService;

/**
 * Implementacion de {@link AuthService}. Toda cuenta nueva nace con rol
 * USUARIO y estado ACTIVO; solo un ADMIN puede promover a otro usuario.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RecursoDuplicadoException("El correo ya esta registrado.");
        }
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new RecursoDuplicadoException("El nombre de usuario '" + username + "' ya esta en uso.");
        }

        Usuario usuario = new Usuario(request.nombre().trim(), request.apellido().trim(), username, email,
                passwordEncoder.encode(request.password()));
        Usuario guardado = usuarioRepository.save(usuario);

        return AuthResponse.bearer(jwtService.generarToken(guardado), UsuarioResponse.desde(guardado));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse iniciarSesion(LoginRequest request) {
        String identificador = request.identificador().trim();

        Usuario usuario = (identificador.contains("@")
                ? usuarioRepository.findByEmailIgnoreCase(identificador)
                : usuarioRepository.findByUsernameIgnoreCase(identificador))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.estaActivo()) {
            throw new AccesoDenegadoException("Tu cuenta esta inactiva. Comunicate con el administrador.");
        }

        return AuthResponse.bearer(jwtService.generarToken(usuario), UsuarioResponse.desde(usuario));
    }
}
