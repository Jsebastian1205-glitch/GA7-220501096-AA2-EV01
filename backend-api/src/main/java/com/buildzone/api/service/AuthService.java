package com.buildzone.api.service;

import com.buildzone.api.dto.AuthResponse;
import com.buildzone.api.dto.LoginRequest;
import com.buildzone.api.dto.RegistroRequest;

/** Casos de uso de autenticacion: registrarse e iniciar sesion. */
public interface AuthService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse iniciarSesion(LoginRequest request);
}
