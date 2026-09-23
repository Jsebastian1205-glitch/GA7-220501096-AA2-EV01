package com.buildzone.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildzone.api.dto.AuthResponse;
import com.buildzone.api.dto.LoginRequest;
import com.buildzone.api.dto.RegistroRequest;
import com.buildzone.api.service.AuthService;

import jakarta.validation.Valid;

/** Endpoints publicos de autenticacion. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/registro - crea una cuenta USUARIO y devuelve su token. */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /** POST /api/auth/login - valida credenciales y devuelve un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }
}
